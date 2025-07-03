package met.agiles.licencias.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import met.agiles.licencias.dto.HolderRequestDto;
import met.agiles.licencias.enums.LicenseClass;
import met.agiles.licencias.persistance.models.Holder;
import met.agiles.licencias.persistance.repository.HolderRepository;
import met.agiles.licencias.services.HolderService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class HolderController {

    private final HolderService holderService;
    private final HolderRepository holderRepository;

    public HolderController(HolderService holderService, HolderRepository holderRepository) {
        this.holderService = holderService;
        this.holderRepository = holderRepository;
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

    @GetMapping("/api/holder/{dni}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getHolderByDni(@PathVariable String dni) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Holder> holder = holderRepository.findById(dni);
            if (holder.isPresent()) {
                response.put("success", true);
                response.put("data", holder.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Titular no existente");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/administrativo/titulares/gestionar")
    public String showHoldersList(Model model) {
        model.addAttribute("holders", holderRepository.findAll());
        model.addAttribute("title", "Gestión de Titulares");
        return "administrativo/titulares/holdersList";
    }
}
