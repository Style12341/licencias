package met.agiles.licencias.controllers;

import met.agiles.licencias.enums.BloodType;
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
import java.util.List;
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
        model.addAttribute("title", "Listado de Titulares");
        return "administrativo/titulares/holdersList";
    }

    @GetMapping("/administrativo/titulares/list")
    public String showHoldersWithFilter(
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BloodType bloodType,
            @RequestParam(required = false) Boolean donor,
            Model model){

        List<Holder> holders = holderService.searchFilteredHolders(dni, name, lastName, city, bloodType, donor);

        model.addAttribute("title", "Listado de Titulares");
        model.addAttribute("holders", holders);
        model.addAttribute("dni", dni);
        model.addAttribute("name", name);
        model.addAttribute("lastName", lastName);
        model.addAttribute("city", city);
        model.addAttribute("bloodType", bloodType);
        model.addAttribute("donor", donor);

        return "administrativo/titulares/holdersList";
    }

    @GetMapping("/administrativo/titulares/{dni}")
    public String mostrarFormularioEdicion(@PathVariable String dni, Model model) {
        try {
            Holder holder = holderService.getHolderByDni(dni);
            if(holder == null) {
                System.out.println("Titular no encontrado");
                return "redirect:/administrativo/titulares/editar-error?error=holdernotfound";
            }

            model.addAttribute("holder", holder);
            return "administrativo/titulares/edit";

        } catch (Exception e) {
            model.addAttribute("error", "No se pudo encontrar el titular con DNI: " + dni);
            System.out.println("Error al buscar el titular: " + e.getMessage());
            return "redirect:/administrativo/titulares/editar-error?error=internal_error";
        }
    }

    @PostMapping("/administrativo/titulares/editar/{dni}")
    public String actualizarTitular(@PathVariable String dni,
                                    @ModelAttribute("holder") @Valid HolderRequestDto holder,
                                    BindingResult result,
                                    Model model) {

        if (result.hasErrors()) {
            // En caso de errores, volver a pasar el objeto al modelo
            model.addAttribute("holder", holder);
            return "administrativo/titulares/edit";
        }

        try {
            holderService.updateHolder(dni, holder);
            System.out.println("Titular actualizado correctamente: \n" + holder);
            return "redirect:/administrativo/titulares/gestionar";

        } catch (Exception e) {
            System.out.println("Error al actualizar el titular: " + e.getMessage());
            return "redirect:/administrativo/titulares/editar-error";
        }
    }

    @GetMapping("/administrativo/titulares/editar-error")
    public String showLicenseReceiptError(@RequestParam(name = "error") String errorType, Model model) {
        switch (errorType) {
            case "holdernotfound":
                model.addAttribute("errorMessage", "Error: Titular no encontrado.");
                break;
            case "internal_error":
                model.addAttribute("errorMessage", "Ha ocurrido un error interno al editar el titular.");
                break;
            default:
                model.addAttribute("errorMessage", "Ha ocurrido un error inesperado.");
                break;
        }
        model.addAttribute("title", "Error al Editar Titular");
        return "administrativo/titulares/edit";
    }

}
