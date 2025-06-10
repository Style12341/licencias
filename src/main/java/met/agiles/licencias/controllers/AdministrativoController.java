package met.agiles.licencias.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import met.agiles.licencias.persistance.models.Holder;
import met.agiles.licencias.persistance.models.License;
import met.agiles.licencias.persistance.models.User;
import met.agiles.licencias.persistance.repository.HolderRepository;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import met.agiles.licencias.services.LicenseService;

@Controller
@RequestMapping("/administrativo")
public class AdministrativoController {

    @Autowired
    private HolderRepository holderService;

    @GetMapping("/home")
    public String administrativoHome(Model model) {
        model.addAttribute("title", "Panel de Administrativo");
        return "administrativo/home";
    }
    
    @Autowired
    private LicenseService licenseService;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

        // Set the issuance date to the current date
        license.setIssuanceDate(LocalDate.now());

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

    @GetMapping("/licencias/imprimir")
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

}