package met.agiles.licencias.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import met.agiles.licencias.dto.HolderRequestDto;
import met.agiles.licencias.enums.LicenseClass;
import met.agiles.licencias.services.HolderService;

@Controller
public class HolderController {

    private final HolderService holderService;

    public HolderController(HolderService holderService) {
        this.holderService = holderService;
    }

    @GetMapping("/administrativo/titulares/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("holder", new HolderRequestDto());
        model.addAttribute("title", "Alta de Titular");
        model.addAttribute("licenseClasses", LicenseClass.values()); 
        return "administrativo/titulares/nuevo";
    }


    @PostMapping("/administrativo/titulares/nuevo")
    public String procesarFormulario(@ModelAttribute("holder") @Valid HolderRequestDto dto,
                                     BindingResult result,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {
        if (result.hasErrors()) {
            model.addAttribute("title", "Alta de Titular");
            return "administrativo/titulares/nuevo";
        }

        try {
            holderService.createHolder(dto, userDetails.getUsername());
            return "redirect:/administrativo/home";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("title", "Alta de Titular");
            return "administrativo/titulares/nuevo";
        }
    }

}
