package br.com.projetosecsr.aluguelequipamentos.devolucao.response;

import java.time.OffsetDateTime;

import br.com.projetosecsr.aluguelequipamentos.devolucao.entidade.Devolucao;

public record DevolucaoListagemResponse(
        Long id,
        Long reservaId,
        OffsetDateTime dataHoraDevolucao,
        String usuarioResponsavelNome
) {

    public static DevolucaoListagemResponse de(Devolucao devolucao) {
        return new DevolucaoListagemResponse(
                devolucao.getId(),
                devolucao.getReserva().getId(),
                devolucao.getDataHoraDevolucao(),
                devolucao.getResponsavel().getNome()
        );
    }
}