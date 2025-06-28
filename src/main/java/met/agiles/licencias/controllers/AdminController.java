package met.agiles.licencias.controllers;

import met.agiles.licencias.enums.Role;
import met.agiles.licencias.persistance.models.User;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import met.agiles.licencias.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private UserService userService;

    @GetMapping("/home")
    public String adminHome(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found for username: " + username));
        
        model.addAttribute("title", "Panel de Administrador");
        model.addAttribute("usuario", user);
        
        // Agregar estadísticas o datos adicionales para el panel de admin
        long totalUsuarios = usuarioRepository.count();
        long totalAdministrativos = usuarioRepository.countByRole(Role.ADMINISTRATIVO);
        
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalAdministrativos", totalAdministrativos);
        
        return "admin/home";
    }

    @GetMapping("/users")
    public String mostrarGestionUsuarios(
            @RequestParam(value="nombre", required=false) String nombre,
            Model model,
            Principal principal
    ) {
        List<User> administrativos;
        if (nombre != null && !nombre.trim().isEmpty()) {
            administrativos = userService.buscarPorNombre(nombre.trim());
        } else {
            administrativos = userService.getAllUsers();
        }
        model.addAttribute("administrativos", administrativos);
        model.addAttribute("currentUsername", principal.getName());
        model.addAttribute("nombre", nombre);   // para repoblar el input
        model.addAttribute("title", "Gestión de Usuarios");
        return "admin/manageUsers";
    }


    @GetMapping("/users/new")
    public String mostrarFormularioAlta(Model model) {
        model.addAttribute("usuario", new User());
        model.addAttribute("roles", Role.values());
        model.addAttribute("title", "Alta de Usuario");
        return "admin/userForm";
    }

    @GetMapping("/admin/users")
    public String listarUsuarios(@RequestParam(required = false) String nombre, Model model, Principal principal) {
        List<User> administrativos;

        if (nombre != null && !nombre.trim().isEmpty()) {
            administrativos = userService.buscarPorNombre(nombre.trim());
        } else {
            administrativos = userService.getAllUsers();
        }

        model.addAttribute("administrativos", administrativos);
        model.addAttribute("currentUsername", principal.getName());
        return "admin/usuarios";
    }

    @PostMapping("/users/save")
    public String guardarUsuario(@ModelAttribute("usuario") User usuario, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            userService.createUser(usuario, userDetails.getUsername());
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {

            String msg = e.getMessage().toLowerCase();
            if (msg.contains("obligatorio")) {
                model.addAttribute("usernameRequiredError", true);
            } else if (msg.contains("ya existe")) {
                model.addAttribute("usernameExistsError", true);
            } else if (msg.contains("nombre")) {
                model.addAttribute("firstNameError", true);
            } else if (msg.contains("apellido")) {
                model.addAttribute("lastNameError", true);
            } else if (msg.contains("contraseña")) {
                model.addAttribute("passwordError", true);
            } else if (msg.contains("rol")) {
                model.addAttribute("roleError", true);
            }

            model.addAttribute("roles", Role.values());
            model.addAttribute("title", "Alta de Usuario");
            return "admin/userForm";
        }
    }

    @PostMapping("/users/delete/{id}")
    public String eliminarUsuario(@PathVariable Long id, Principal principal) {
        User userToDelete = userService.getUserById(id);
        if (userToDelete != null && userToDelete.getUsername().equals(principal.getName())) {
            // No permitir eliminarse a sí mismo
            return "redirect:/admin/users?error=NoSePuedeEliminarASiMismo";
        }
        // Llamamos al servicio que marca active = false
        userService.disableUser(id);
        return "redirect:/admin/users";
    }


    @GetMapping("/users/edit/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/admin/users?error=UsuarioNoEncontrado";
        }
        model.addAttribute("usuario", user);
        return "admin/editUserForm";
    }

    @PostMapping("/users/update")
    public String actualizarUsuario(@ModelAttribute("usuario") User userForm, Principal principal) {
        userService.actualizarDatosUsuario(userForm, principal.getName());
        return "redirect:/admin/users?success=modificado";
    }

}