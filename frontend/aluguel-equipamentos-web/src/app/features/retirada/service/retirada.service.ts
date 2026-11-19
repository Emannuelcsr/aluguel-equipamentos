import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PaginaResponse } from '../../../shared/pagina-response';
import { API_URL } from '../../../core/config/api.config';

@Injectable({
  providedIn: 'root'
})
export class RetiradaService {

  constructor(private http: HttpClient) {}


  cadastrar(dados: CadastrarReservaRequest) {
      return this.http.post<ReservaResponse>(
        `${API_URL}/retiradas`,
        dados,
      );
    }
  
    listar(pagina: number, tamanho: number) {
      return this.http.get<PaginaResponse<ReservaResponse>>(
        `${API_URL}/reservas?page=${pagina}&size=${tamanho}`,
      );
    }
  
    buscarPorId(id: number) {
      return this.http.get<ReservaResponse>(`${API_URL}/retiradas/${id}`);
    }
  
}
