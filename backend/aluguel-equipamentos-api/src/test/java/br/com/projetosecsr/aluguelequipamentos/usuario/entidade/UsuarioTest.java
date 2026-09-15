package br.com.projetosecsr.aluguelequipamentos.usuario.entidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class UsuarioTest {

	@Test
	void devePrepararUsuarioParaCriacao() {

		// PREPARAR
		Usuario usuario = new Usuario("João Silva", "joao@email.com", "hash-da-senha", PerfilUsuario.FUNCIONARIO);
		
		//EXECUTAR
		usuario.prepararParaCriacao();
		
		//VERIFICAR
		assertTrue(usuario.isAtivo());
		assertNotNull(usuario.getDataCriacao());
		assertNotNull(usuario.getDataAtualizacao());
		assertEquals(usuario.getDataCriacao(), usuario.getDataAtualizacao());
		
		
	}

}
