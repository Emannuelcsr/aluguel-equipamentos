import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PaginaResponse } from '../../../shared/pagina-response';
import { ClienteResponse } from '../../../core/models/cliente-response';
import { CadastrarClienteRequest } from '../../../core/models/cadastrar-cliente-request';
import { API_URL } from '../../../core/config/api.config';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {

 constructor(private http: HttpClient) { }

listar(pagina: number, tamanho: number) {
  return this.http.get<PaginaResponse<ClienteResponse>>(
    `${API_URL}/clientes?page=${pagina}&size=${tamanho}`
  );
}

cadastrar(dados: CadastrarClienteRequest) {
  return this.http.post<ClienteResponse>(
    `${API_URL}/clientes`,
    dados
  );
}


}
