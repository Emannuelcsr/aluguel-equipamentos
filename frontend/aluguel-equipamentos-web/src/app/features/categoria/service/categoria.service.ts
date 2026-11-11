import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CategoriaResponse } from '../../../core/models/categoria-response';
import { API_URL } from '../../../core/config/api.config';
import { CadastrarCategoriaRequest } from '../../../core/models/cadastrar-categoria-request';
import { PaginaResponse } from '../../../shared/pagina-response';
import { AtualizarCategoriaRequest } from '../../../core/models/atualizar-categoria-request';

@Injectable({
  providedIn: 'root',
})
export class CategoriaService {
  constructor(private http: HttpClient) {}

  cadastrar(dados: CadastrarCategoriaRequest) {
    return this.http.post<CategoriaResponse>(`${API_URL}/categorias`, dados);
  }

  listar(pagina: number, tamanho: number) {
    return this.http.get<PaginaResponse<CategoriaResponse>>(
      `${API_URL}/categorias?page=${pagina}&size=${tamanho}&sort=id,asc`,
    );
  }

  buscarPorId(id: number) {
    return this.http.get<CategoriaResponse>(`${API_URL}/categorias/${id}`);
  }

  editarCategoria(id: number, dados: AtualizarCategoriaRequest) {
    return this.http.put<CategoriaResponse>(
      `${API_URL}/categorias/${id}`,
      dados,
    );
  }

  ativarCategoria(id: number) {
    return this.http.patch<CategoriaResponse>(
      `${API_URL}/categorias/${id}/ativar`,
      null,
    );
  }

  desativarCategoria(id: number) {
    return this.http.patch<CategoriaResponse>(
      `${API_URL}/categorias/${id}/desativar`,
      null,
    );
  }

}
