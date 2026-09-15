package br.com.projetosecsr.aluguelequipamentos.usuario.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	boolean existsByEmail(String email);

	Optional<Usuario> findByEmail(String email);
}
