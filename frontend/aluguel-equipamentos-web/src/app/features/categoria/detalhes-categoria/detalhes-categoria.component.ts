import { Component, OnInit } from '@angular/core';
import { CategoriaResponse } from '../../../core/models/categoria-response';
import { CategoriaService } from '../service/categoria.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AutenticacaoService } from '../../../core/services/autenticacao.service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-detalhes-categoria',
  imports: [RouterLink, DatePipe],
  templateUrl: './detalhes-categoria.component.html',
  styleUrl: './detalhes-categoria.component.scss',
})
export class DetalhesCategoriaComponent implements OnInit {
  categoria!: CategoriaResponse;
  mensagemErro = '';
  carregando = false;
  id!: number;
  desativando = false;
  ativando = false;
  ehAdministrador = false;
  mensagemSucesso = '';

  constructor(
    private categoriaService: CategoriaService,
    private route: ActivatedRoute,
    private autenticacaoService: AutenticacaoService,
  ) {}
  ngOnInit(): void {
    if (this.autenticacaoService.obterUsuario()?.perfil === 'ADMINISTRADOR') {
      this.ehAdministrador = true;
    } else {
      this.ehAdministrador = false;
    }

    const idParametro = this.route.snapshot.paramMap.get('id');

    if (idParametro) {
      this.id = Number(idParametro);

      if (!Number.isInteger(this.id) || this.id <= 0) {
        this.mensagemErro = 'ID da categoria inválido.';
        return;
      }

      this.carregando = true;
      this.mensagemErro = '';

      this.categoriaService.buscarPorId(this.id).subscribe({
        next: (resposta) => {
          this.mensagemErro = '';
          this.carregando = false;
          this.categoria = resposta;
        },
        error: (erro) => {
          this.carregando = false;
          this.mensagemSucesso = '';

          if (erro.status === 0) {
            this.mensagemErro =
              'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
            return;
          }

          this.mensagemErro =
            erro.error?.mensagens?.[0] ??
            'Não foi possível carregar os dados da categoria.';
        },
      });
    } else {
      this.mensagemErro = 'ID da categoria não informado.';
    }
  }

  desativarCategoria() {
    if (this.desativando) {
      return;
    }

    const confirmou = confirm('Deseja realmente desativar esta categoria?');

    if (!confirmou) {
      return;
    }

    this.desativando = true;
    this.mensagemErro = '';
    this.mensagemSucesso = '';
    this.categoriaService.desativarCategoria(this.id).subscribe({
      next: (resposta) => {
        this.mensagemErro = '';
        this.mensagemSucesso = 'Categoria desativada com sucesso!';
        this.desativando = false;
        this.categoria.ativo = resposta.ativo;
      },
      error: (erro) => {
        this.desativando = false;
        this.mensagemSucesso = '';
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ??
          'Não foi possível desativar a categoria.';
      },
    });
  }

  ativarCategoria() {
    if (this.ativando) {
      return;
    }

    const confirmou = confirm('Deseja realmente ativar esta categoria?');

    if (!confirmou) {
      return;
    }
    this.ativando = true;
    this.mensagemErro = '';
    this.mensagemSucesso = '';
    this.categoriaService.ativarCategoria(this.id).subscribe({
      next: (resposta) => {
        this.mensagemErro = '';
        this.mensagemSucesso = 'Categoria ativada com sucesso!';
        this.ativando = false;
        this.categoria.ativo = resposta.ativo;
      },
      error: (erro) => {
        this.ativando = false;

        this.mensagemSucesso = '';
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ?? 'Não foi possível ativar a categoria.';
      },
    });
  }
}
