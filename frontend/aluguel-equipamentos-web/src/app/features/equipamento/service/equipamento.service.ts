import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CadastrarEquipamentoRequest } from '../../../core/models/cadastrar-equipamento-request';
import { EquipamentoResponse } from '../../../core/models/equipamento-response';
import { API_URL } from '../../../core/config/api.config';
import { PaginaResponse } from '../../../shared/pagina-response';
import { AtualizarEquipamentoRequest } from '../../../core/models/atualizar-equipamento-request';

@Injectable({
  providedIn: 'root',
})
export class EquipamentoService {
  constructor(private http: HttpClient) {}

  cadastrar(dados: CadastrarEquipamentoRequest) {
    return this.http.post<EquipamentoResponse>(
      `${API_URL}/equipamentos`,
      dados,
    );
  }

  listar(pagina: number, tamanho: number) {
    return this.http.get<PaginaResponse<EquipamentoResponse>>(
      `${API_URL}/equipamentos?page=${pagina}&size=${tamanho}&sort=id,asc`,
    );
  }

  buscarPorId(id: number) {
    return this.http.get<EquipamentoResponse>(`${API_URL}/equipamentos/${id}`);
  }

  ativarEquipamento(id: number) {
    return this.http.patch<EquipamentoResponse>(
      `${API_URL}/equipamentos/${id}/ativar`,
      null,
    );
  }

  desativarEquipamento(id: number) {
    return this.http.patch<EquipamentoResponse>(
      `${API_URL}/equipamentos/${id}/desativar`,
      null,
    );
  }

    editarEquipamento(id: number, dados: AtualizarEquipamentoRequest) {
      return this.http.put<EquipamentoResponse>(
        `${API_URL}/equipamentos/${id}`,
        dados,
      );
    }
}


