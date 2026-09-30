package br.com.projetosecsr.aluguelequipamentos.cliente.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

	boolean existsByDocumento(String documento);

	boolean existsByEmail(String email);

}