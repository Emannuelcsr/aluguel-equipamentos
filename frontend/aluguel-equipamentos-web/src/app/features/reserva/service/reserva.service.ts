import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CadastrarReservaRequest } from '../../../core/models/cadastrar-reserva-request';
import { ReservaResponse } from '../../../core/models/reserva-response';
import { API_URL } from '../../../core/config/api.config';
import { PaginaResponse } from '../../../shared/pagina-response';

@Injectable({
  providedIn: 'root'
})
export class ReservaService {
constructor(private http: HttpClient) {}

  cadastrar(dados: CadastrarReservaRequest) {
    return this.http.post<ReservaResponse>(
      `${API_URL}/reservas`,
      dados,
    );
  }

  listar(pagina: number, tamanho: number) {
    return this.http.get<PaginaResponse<ReservaResponse>>(
      `${API_URL}/reservas?page=${pagina}&size=${tamanho}`,
    );
  }

  buscarPorId(id: number) {
    return this.http.get<ReservaResponse>(`${API_URL}/reservas/${id}`);
  }

  listarAprovadas(pagina: number, tamanho: number) {
    return this.http.get<PaginaResponse<ReservaResponse>>(
      `${API_URL}/reservas/aprovadas?page=${pagina}&size=${tamanho}`,
    );
  }



  aprovar(id: number) {
    return this.http.patch<ReservaResponse>(
      `${API_URL}/reservas/${id}/aprovar`,
      null,
    );
  }

  rejeitar(id: number) {
    return this.http.patch<ReservaResponse>(
      `${API_URL}/reservas/${id}/rejeitar`,
      null,
    );
  }

    cancelar(id: number) {
    return this.http.patch<ReservaResponse>(
      `${API_URL}/reservas/${id}/cancelar`,
      null,
    );
  }


}


