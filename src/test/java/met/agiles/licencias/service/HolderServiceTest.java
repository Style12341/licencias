package met.agiles.licencias.service;

import met.agiles.licencias.dto.HolderRequestDto;
import met.agiles.licencias.persistance.models.Holder;
import met.agiles.licencias.persistance.models.User;
import met.agiles.licencias.persistance.repository.HolderRepository;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import met.agiles.licencias.services.HolderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HolderServiceTest {

    @Mock
    private HolderRepository holderRepository;

    @Mock
    private UsuarioRepository userRepository;

    @InjectMocks
    private HolderService holderService;

    @Test
    void noDebeCrearSiDniExiste() {
        HolderRequestDto dto = new HolderRequestDto();
        dto.setDni("12345678");

        when(holderRepository.existsById("12345678")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                holderService.createHolder(dto, "adminUser"));
    }

    @Test
    void noDebeCrearSiClaseInvalida() {
        HolderRequestDto dto = new HolderRequestDto();
        dto.setDni("123");

        when(holderRepository.existsById("123")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                holderService.createHolder(dto, "adminUser"));
    }

    @Test
    void debeCrearTitularCorrectamente() {
        HolderRequestDto dto = new HolderRequestDto();
        dto.setDni("12345678");
        dto.setCuil("20-12345678-9");
        dto.setName("Juan");
        dto.setLastName("Pérez");
        dto.setAddress("Av. Siempre Viva 123");
        dto.setCity("Springfield");
        dto.setBirthDate(LocalDate.of(2000, 1, 1));
        // dto.setLicenseClass(Class.B);
        dto.setBloodType("O+");
        dto.setDonor(true);

        User user = new User();
        user.setUsername("adminUser");

        when(holderRepository.existsById("12345678")).thenReturn(false);
        when(userRepository.findByUsername("adminUser")).thenReturn(Optional.of(user));
        when(holderRepository.save(org.mockito.ArgumentMatchers.any(Holder.class))).thenAnswer(i -> i.getArgument(0));

        Holder saved = holderService.createHolder(dto, "adminUser");

        assertEquals("Juan", saved.getName());
        assertEquals(user, saved.getAdministrative());
    }
}