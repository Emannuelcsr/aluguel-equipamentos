package br.com.projetosecsr.aluguelequipamentos.cliente.request;

import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.TipoCliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastrarClienteRequest(

        @NotNull
        TipoCliente tipo,

        @NotBlank
        @Size(min = 3, max = 150)
        String nomeRazaoSocial,

        @Size(max = 150)
        String nomeFantasia,

        @NotBlank
        @Size(min = 11, max = 14)
        String documento,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @NotBlank
        @Pattern(regexp = "\\d{10,11}")
        String telefone,

        @NotBlank
        @Pattern(regexp = "\\d{8}")
        String cep,

        @NotBlank
        @Size(max = 150)
        String logradouro,

        @NotBlank
        @Size(max = 20)
        String numero,

        @Size(max = 100)
        String complemento,

        @NotBlank
        @Size(max = 100)
        String bairro,

        @NotBlank
        @Size(max = 100)
        String cidade,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{2}")
        String estado

) {

}