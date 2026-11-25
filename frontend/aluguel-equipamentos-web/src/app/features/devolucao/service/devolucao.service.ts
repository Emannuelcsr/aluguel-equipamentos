import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CadastrarDevolucaoRequest } from '../../../core/models/cadastrar-devolucao-request';
import { API_URL } from '../../../core/config/api.config';
import { DevolucaoResponse } from '../../../core/models/devolucao-response';
import { PaginaResponse } from '../../../shared/pagina-response';

@Injectable({
  providedIn: 'root',
})
export class DevolucaoService {
  constructor(private http: HttpClient) {}

  cadastrar(dados: CadastrarDevolucaoRequest) {
    return this.http.post<DevolucaoResponse>(`${API_URL}/devolucoes`, dados);
  }

  listar(pagina: number, tamanho: number) {
    return this.http.get<PaginaResponse<DevolucaoResponse>>(
      `${API_URL}/devolucoes?page=${pagina}&size=${tamanho}`,
    );
  }

  buscarPorId(id: number) {
    return this.http.get<DevolucaoResponse>(`${API_URL}/devolucoes/${id}`);
  }
}
