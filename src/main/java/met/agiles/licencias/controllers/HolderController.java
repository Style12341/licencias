package met.agiles.licencias.controllers;

import jakarta.validation.Valid;
import met.agiles.licencias.dto.HolderRequestDto;
import met.agiles.licencias.persistance.models.Holder;
import met.agiles.licencias.services.HolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/holders")
public class HolderController {

    @Autowired
    private HolderService holderService;

    @PostMapping
    public ResponseEntity<?> altaTitular(@RequestBody @Valid HolderRequestDto dto,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Holder nuevo = holderService.createHolder(dto, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        }
    }
}
