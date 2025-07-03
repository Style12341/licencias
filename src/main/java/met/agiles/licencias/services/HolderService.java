package met.agiles.licencias.services;

import met.agiles.licencias.dto.HolderRequestDto;
import met.agiles.licencias.enums.BloodType;
import met.agiles.licencias.persistance.models.Holder;
import met.agiles.licencias.persistance.models.User;
import met.agiles.licencias.persistance.repository.HolderRepository;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class HolderService {

    @Autowired
    private HolderRepository holderRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Holder createHolder(HolderRequestDto dto, String adminUsername) {
        if (holderRepository.existsById(dto.getDni())) {
            throw new IllegalArgumentException("Titular con DNI ya registrado");
        }


        BloodType tipoSangre = dto.getBloodType();
        if (tipoSangre == null) {
            throw new IllegalArgumentException("Grupo sanguíneo inválido");
        }

        // Validación de edad mínima (ejemplo: 16 años)
        if (Period.between(dto.getBirthDate(), LocalDate.now()).getYears() < 16) {
            throw new IllegalArgumentException("El titular debe tener al menos 16 años");
        }

        User admin = usuarioRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Administrativo no encontrado"));

        Holder holder = new Holder();
        BeanUtils.copyProperties(dto, holder);
        holder.setAdministrative(admin);

        return holderRepository.save(holder);
    }

    public Holder updateHolder(String dni, HolderRequestDto dto) {
        Holder existingHolder = holderRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Titular inexistente"));

        // Validación de edad mínima (ejemplo: 16 años)
        if (Period.between(dto.getBirthDate(), LocalDate.now()).getYears() < 16) {
            throw new IllegalArgumentException("El titular debe tener al menos 16 años. No es posible especificar esa edad.");
        }

        BeanUtils.copyProperties(dto, existingHolder, "dni", "administrative");
        return holderRepository.save(existingHolder);
    }

    public List<Holder> searchFilteredHolders(String dni, String name, String lastname, String city, BloodType bloodType, Boolean donor) {
        List<Holder> holders = holderRepository.findAll();

        return holders.stream()
                .filter(holder -> (dni == null || dni.isBlank() || holder.getDni().contains(dni)) &&
                                  (name == null || name.isBlank() || holder.getName().toLowerCase().contains(name)) &&
                                  (lastname == null || lastname.isBlank() ||holder.getLastName().toLowerCase().contains(lastname)) &&
                                  (city == null || city.isBlank() || holder.getCity().toLowerCase().contains(city)) &&
                                  (bloodType == null || holder.getBloodType().toString().length() > 10 || holder.getBloodType().toString().equalsIgnoreCase(bloodType.toString())) &&
                                  (donor == null || holder.isDonor() == donor))
                .toList();
    }

    public Holder getHolderByDni(String dni) {
        return holderRepository.findById(dni).orElse(null);
    }

    public List<Holder> getAllHolders() {
        return holderRepository.findAll();
    }
}