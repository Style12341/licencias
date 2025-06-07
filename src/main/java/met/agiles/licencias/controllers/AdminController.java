package met.agiles.licencias.controllers;

import met.agiles.licencias.enums.Role;
import met.agiles.licencias.persistance.models.Usuario;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/home")
    public String adminHome(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        
        model.addAttribute("title", "Panel de Administrador");
        model.addAttribute("usuario", usuario);
        
        // Agregar estadísticas o datos adicionales para el panel de admin
        long totalUsuarios = usuarioRepository.count();
        long totalAdministrativos = usuarioRepository.countByRol(Role.ADMINISTRATIVO);
        
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalAdministrativos", totalAdministrativos);
        
        return "admin/home";
    }
}