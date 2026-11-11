import { Component, OnInit } from '@angular/core';
import { CategoriaResponse } from '../../../core/models/categoria-response';
import { CategoriaService } from '../service/categoria.service';
import { ReactiveFormsModule } from '@angular/forms';
import { NgxMaskDirective, provideNgxMask } from 'ngx-mask';
import { RouterLink } from '@angular/router';
import { AutenticacaoService } from '../../../core/services/autenticacao.service';

@Component({
  selector: 'app-listagem-categorias',
  imports: [RouterLink],
  templateUrl: './listagem-categorias.component.html',
  styleUrl: './listagem-categorias.component.scss',
})
export class ListagemCategoriasComponent implements OnInit {
  categorias: CategoriaResponse[] = [];
  mensagemSucesso = '';
  mensagemErro = '';
  paginaAtual = 1;
  tamanhoPagina = 5;
  totalPaginas = 0;
  primeiraPagina = true;
  ultimaPagina = false;
  totalElementos = 0;
  carregando = false;
  paginaSolicitada = 1;
  ehAdministrador=false;

  constructor(private categoriaService: CategoriaService, private autenticacaoService: AutenticacaoService) {}
  ngOnInit(): void {

    if (this.autenticacaoService.obterUsuario()?.perfil === 'ADMINISTRADOR') {
      this.ehAdministrador = true;
    } else {
      this.ehAdministrador = false;
    }

    this.mensagemSucesso = history.state.mensagemSucesso ?? '';

    if (this.mensagemSucesso) {
      const estadoAtual = { ...history.state };
      delete estadoAtual.mensagemSucesso;

      history.replaceState(estadoAtual, '');
    }

    this.listar(this.paginaAtual);
  }

  listar(pagina: number) {
    if (this.carregando) {
      return;
    }
      this.paginaSolicitada = pagina;
    this.carregando = true;
    this.mensagemErro = '';

    this.categoriaService.listar(pagina, this.tamanhoPagina).subscribe({
      next: (resposta) => {
        this.carregando = false;
        this.categorias = resposta.conteudo;
        this.totalPaginas = resposta.totalPaginas;
        this.primeiraPagina = resposta.primeiraPagina;
        this.ultimaPagina = resposta.ultimaPagina;
        this.totalElementos = resposta.totalElementos;
        this.paginaAtual = resposta.paginaAtual;
      },
      error: (erro) => {
        this.carregando = false;
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ?? 'Não foi possível listar as categorias.';
      },
    });
  }

  proximaPagina() {
    if (!this.ultimaPagina) {
      this.listar(this.paginaAtual + 1);
    }
  }

  voltarPagina() {
    if (this.paginaAtual > 1) {
      this.listar(this.paginaAtual - 1);
    }
  }
}
