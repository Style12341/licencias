package met.agiles.licencias.controllers;

import met.agiles.licencias.enums.LicenseClass;
import met.agiles.licencias.services.HolderService;
import org.springframework.ui.Model;
import jakarta.validation.Valid;
import met.agiles.licencias.dto.HolderRequestDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HolderViewController {

    private final HolderService holderService;

    public HolderViewController(HolderService holderService) {
        this.holderService = holderService;
    }

    @GetMapping("/administrativo/alta-titular")
    public String mostrarFormulario(Model model) {
        model.addAttribute("holder", new HolderRequestDto());
        model.addAttribute("title", "Alta de Titular");
        model.addAttribute("licenseClasses", LicenseClass.values()); // 👈 Agregado
        return "administrativo/alta-titular";
    }


    @PostMapping("/administrativo/alta-titular")
    public String procesarFormulario(@ModelAttribute("holder") @Valid HolderRequestDto dto,
                                     BindingResult result,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {
        if (result.hasErrors()) {
            model.addAttribute("title", "Alta de Titular");
            return "administrativo/alta-titular";
        }

        try {
            holderService.createHolder(dto, userDetails.getUsername());
            return "redirect:/administrativo/home";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("title", "Alta de Titular");
            return "administrativo/alta-titular";
        }
    }

}
