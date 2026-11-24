import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PaginaResponse } from '../../../shared/pagina-response';
import { API_URL } from '../../../core/config/api.config';
import { CadastrarRetiradaRequest } from '../../../core/models/cadastrar-retirada-request';
import { RetiradaResponse } from '../../../core/models/retirada-response';

@Injectable({
  providedIn: 'root'
})
export class RetiradaService {

  constructor(private http: HttpClient) {}


  cadastrar(dados: CadastrarRetiradaRequest) {
      return this.http.post<RetiradaResponse>(
        `${API_URL}/retiradas`,
        dados,
      );
    }
  
    listar(pagina: number, tamanho: number) {
      return this.http.get<PaginaResponse<RetiradaResponse>>(
        `${API_URL}/retiradas?page=${pagina}&size=${tamanho}`,
      );
    }
  
    buscarPorId(id: number) {
      return this.http.get<RetiradaResponse>(`${API_URL}/retiradas/${id}`);
    }
  
}
