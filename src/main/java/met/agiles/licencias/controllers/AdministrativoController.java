package met.agiles.licencias.controllers;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import met.agiles.licencias.services.UsuarioService;

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

        // Check if the holder exists, and assign it to the license
        Optional<Holder> holder = holderService.findById(license.getDni());
        if (holder.isEmpty()) {
            model.addAttribute("holderNotFound", true);
            return "administrativo/issueLicenseForm"; // Return to the form page with validation errors
        }else{
            license.setHolder(holder.get());;
        }

        // Set the issuance date to the current date
        license.setIssuanceDate(new Date(System.currentTimeMillis()));

        // Set the user to the current user logged
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = usuarioRepository.findByUsername(userDetails.getUsername()).orElse(null);
        license.setUser(user);

        // Compute the expiration #TODO
        license.setExpirationDate(new Date(System.currentTimeMillis() + 365L * 24L * 60L * 60L * 1000L));

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