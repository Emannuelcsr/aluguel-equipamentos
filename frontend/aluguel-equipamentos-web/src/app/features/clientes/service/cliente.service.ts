import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PaginaResponse } from '../../../shared/pagina-response';
import { ClienteResponse } from '../../../core/models/cliente-response';
import { CadastrarClienteRequest } from '../../../core/models/cadastrar-cliente-request';
import { API_URL } from '../../../core/config/api.config';
import { AtualizarClienteRequest } from '../../../core/models/atualizar-cliente-request';

@Injectable({
  providedIn: 'root',
})
export class ClienteService {
  constructor(private http: HttpClient) {}

  listar(pagina: number, tamanho: number) {
    return this.http.get<PaginaResponse<ClienteResponse>>(
      `${API_URL}/clientes?page=${pagina}&size=${tamanho}&sort=id,asc`,
    );
  }

  cadastrar(dados: CadastrarClienteRequest) {
    return this.http.post<ClienteResponse>(`${API_URL}/clientes`, dados);
  }

  buscarPorId(id: number) {
    return this.http.get<ClienteResponse>(`${API_URL}/clientes/${id}`);
  }

  editarCliente(id: number, dados: AtualizarClienteRequest) {
    return this.http.put<ClienteResponse>(`${API_URL}/clientes/${id}`, dados);
  }

  ativarCliente(id: number) {
    return this.http.patch<ClienteResponse>(
      `${API_URL}/clientes/${id}/ativar`,
      null,
    );
  }

  desativarCliente(id: number) {
    return this.http.patch<ClienteResponse>(
      `${API_URL}/clientes/${id}/desativar`,
      null,
    );
  }
}
