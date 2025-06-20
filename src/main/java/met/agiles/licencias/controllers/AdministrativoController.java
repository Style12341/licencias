package met.agiles.licencias.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.lowagie.text.DocumentException;
import met.agiles.licencias.enums.PaymentMethod;
import met.agiles.licencias.services.PdfGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import met.agiles.licencias.persistance.models.Holder;
import met.agiles.licencias.persistance.models.License;
import met.agiles.licencias.persistance.models.User;
import met.agiles.licencias.persistance.repository.HolderRepository;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import met.agiles.licencias.services.LicenseReportService;
import met.agiles.licencias.services.LicenseService;

@Controller
@RequestMapping("/administrativo")
public class AdministrativoController {

    @Autowired
    private HolderRepository holderService;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    private static final Logger logger = LoggerFactory.getLogger(AdministrativoController.class);
    
    @Autowired
    private LicenseService licenseService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/home")
    public String administrativoHome(Model model) {
        model.addAttribute("title", "Panel de Administrativo");
        return "administrativo/home";
    }

    @GetMapping("/licencias/emitir")
    public String issueLicenseForm(Model model) {
        model.addAttribute("license", new License());
        model.addAttribute("title", "Emitir Licencia");
        return "administrativo/issueLicenseForm";
    }
    
    @GetMapping("/licencias")
    public ResponseEntity<List<License>> getAllLicenses() {
        return ResponseEntity.ok(licenseService.getAllLicenses());
    }
    
    @GetMapping("/licencias/{id}")
    public ResponseEntity<License> getLicenseById(@PathVariable Long id) {
        return ResponseEntity.ok(licenseService.getLicenseById(id));
    }
    
    @PostMapping("/licencias")
    public String createLicense(@Valid @ModelAttribute License license, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "administrativo/issueLicenseForm"; // Return to the form page with validation errors
        }

        LocalDate birthDate = license.getBirthDate();
        // Check if the birth date allows to issue a license today
        if (!licenseService.isValidBirthDateWindow(birthDate)){
            model.addAttribute("invalidBirthDateWindow", true);
            return "administrativo/issueLicenseForm";
        }

        // Check if the holder is old enough to issue a license
        if (!licenseService.isValidAge(license.getBirthDate(), license.getLicenseClasses())){
            model.addAttribute("invalidAge", true);
            return "administrativo/issueLicenseForm";
        }

        // Check if the holder can have a first time professional license
        if (!licenseService.isValidFirstTimeForProfessionalLicense(license.getDni(), license.getBirthDate(), license.getLicenseClasses())) {
            model.addAttribute("invalidFirstTimeForProfessionalLicense", true);
            return "administrativo/issueLicenseForm";
        }

        // Set the issuance date to the holders birthday date
        license.setIssuanceDate(birthDate.withYear(LocalDate.now().getYear()));

        // Set the user to the current user logged
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = usuarioRepository.findByUsername(userDetails.getUsername()).orElse(null);
        license.setUser(user);

        // Check if the holder exists, and assign it to the license. Do not use holder entity data to compute validations.
        Optional<Holder> holder = holderService.findById(license.getDni());
        if (holder.isEmpty()) {
            model.addAttribute("holderNotFound", true);
            return "administrativo/issueLicenseForm";
        }else{
            license.setHolder(holder.get());
        }

        if (licenseService.isFirstLicense(license.getDni())) {
            license.setObvservations("Principiante por primeros 6 meses. "+license.getObvservations());
        }

        licenseService.createLicense(license);
        return "redirect:/administrativo/home"; // Redirect to the list page after successful creation
    }
    
    @PutMapping("/licencias/{id}")
    public ResponseEntity<License> updateLicense(@PathVariable Long id, @RequestBody License license) {
        return ResponseEntity.ok(licenseService.updateLicense(license));
    }
    
    @DeleteMapping("/licencias/{id}")
    public ResponseEntity<Void> deleteLicense(@PathVariable Long id) {
        licenseService.deleteLicense(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/licencias/list")
    public String mostrarLicenciasConFiltro(
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false, defaultValue = "asc") String orden,
            Model model) {

        List<License> licencias = licenseService.searchFilteredLicenses(dni, apellido, orden);

        model.addAttribute("title", "Listado de Licencias");
        model.addAttribute("licencias", licencias);
        model.addAttribute("dni", dni);
        model.addAttribute("apellido", apellido);
        model.addAttribute("orden", orden);

        return "administrativo/licensesList";
    }

    @GetMapping("/licencias/buscar")
    public String showAndSearchLicensesPage(
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false, defaultValue = "asc") String orden,
            Model model) {

        List<License> licencias = Collections.emptyList();

        boolean realizarBusqueda = (dni != null && !dni.isBlank()) || (apellido != null && !apellido.isBlank());

        if (realizarBusqueda) {
            licencias = licenseService.searchFilteredLicenses(dni, apellido, orden);
        }

        model.addAttribute("title", "Buscar Licencia");
        model.addAttribute("licencias", licencias);
        model.addAttribute("dni", dni);
        model.addAttribute("apellido", apellido);
        model.addAttribute("orden", orden);

        model.addAttribute("busquedaRealizada", realizarBusqueda);

        return "administrativo/searchLicenses";
    }

    @GetMapping("licencias/imprimir/{id}")
    public String showPrintLicensePage(@PathVariable Long id, Model model) {

        try {
            License licencia = licenseService.getLicenseById(id);
            if (licencia == null) {
                // Manejar el caso en que la licencia no se encuentre
                return "redirect:/administrativo/licencias/buscar?error=notfound";
            }

            model.addAttribute("licencia", licencia);
            model.addAttribute("title", "Imprimir Licencia - " + licencia.getLast_name());
            model.addAttribute("paymentMethods", Arrays.asList(PaymentMethod.values()));

            return "administrativo/licenseToPrint";
        } catch (RuntimeException e) {
            // Manejar excepciones de tiempo de ejecución
            return "redirect:/administrativo/licencias/buscar?error=internal_error";
        }
    }

    @GetMapping("/licencias/generar-pdf/{id}")
    public ResponseEntity<byte[]> generarLicenciaPdf(@PathVariable Long id) {
        try {
            License licencia = licenseService.getLicenseById(id);
            if (licencia == null) {
                return ResponseEntity.notFound().build();
            }

            // La llamada a tu servicio de generación de PDF es la misma
            byte[] pdfBytes = pdfGeneratorService.generateLicensePdf(licencia);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "licencia_" + licencia.getId() + "_" + licencia.getHolder().getLastName().replace(" ", "_") + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/licencias/guardar-metodo-pago/{id}")
    public ResponseEntity<String> savePaymentMethod(
            @PathVariable("id") Long id,
            @RequestParam("paymentMethod") PaymentMethod paymentMethod) {
        try {
            licenseService.assignPaymentToLicense(id, paymentMethod);
            logger.info("Método de pago {} asignado a la licencia ID {}", paymentMethod, id);
            return new ResponseEntity<>("Método de pago guardado exitosamente", HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error al asignar el método de pago {} a la licencia ID {}: {}", paymentMethod, id, e.getMessage());
            return new ResponseEntity<>("Error al guardar el método de pago", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Autowired
    private LicenseReportService licenseReportService;

    @GetMapping("/licencias/reporte/expiradas")
    public String showExpirationDateRangesForm(Model model) {
        return "administrativo/licenseExpirationReport";
    }

    @PostMapping("/license-report-by-expiration")
    public void generateReport(@RequestParam("startDate") LocalDate startDate, @RequestParam("endDate") LocalDate endDate, HttpServletResponse response) throws IOException {
        licenseReportService.generateReport(startDate, endDate, response);
    }
}