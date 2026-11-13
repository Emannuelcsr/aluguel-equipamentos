import { Injectable } from '@angular/core';
import { UnidadeEquipamentoResponse } from '../../../core/models/unidade-equipamento-response';
import { CadastrarUnidadeEquipamentoRequest } from '../../../core/models/cadastrar-unidade-equipamento-request';
import { PaginaResponse } from '../../../shared/pagina-response';
import { API_URL } from '../../../core/config/api.config';

import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class UnidadeEquipamentoService {
  constructor(private http: HttpClient) {}

  cadastrar(dados: CadastrarUnidadeEquipamentoRequest) {
    return this.http.post<UnidadeEquipamentoResponse>(
      `${API_URL}/unidades-equipamento`,
      dados,
    );
  }

  listar(pagina: number, tamanho: number) {
    return this.http.get<PaginaResponse<UnidadeEquipamentoResponse>>(
      `${API_URL}/unidades-equipamento?page=${pagina}&size=${tamanho}&sort=id,asc`,
    );
  }

  buscarPorId(id: number) {
    return this.http.get<UnidadeEquipamentoResponse>(
      `${API_URL}/unidades-equipamento/${id}`,
    );
  }

  ativarUnidadeEquipamento(id: number) {
    return this.http.patch<UnidadeEquipamentoResponse>(
      `${API_URL}/unidades-equipamento/${id}/ativar`,
      null,
    );
  }

  desativarUnidadeEquipamento(id: number) {
    return this.http.patch<UnidadeEquipamentoResponse>(
      `${API_URL}/unidades-equipamento/${id}/desativar`,
      null,
    );
  }

  enviarUnidadeEquipamentoManutencao(id: number) {
    return this.http.patch<UnidadeEquipamentoResponse>(
      `${API_URL}/unidades-equipamento/${id}/manutencao`,
      null,
    );
  }

  liberarUnidadeEquipamentoDaManutencao(id: number) {
    return this.http.patch<UnidadeEquipamentoResponse>(
      `${API_URL}/unidades-equipamento/${id}/liberar-manutencao`,
      null,
    );
  }

listarPorEquipamento(
  equipamentoId: number,
  pagina: number,
  tamanho: number,
) {
  return this.http.get<PaginaResponse<UnidadeEquipamentoResponse>>(
    `${API_URL}/unidades-equipamento/equipamento/${equipamentoId}?page=${pagina}&size=${tamanho}&sort=id,asc`,
  );
}

  }

