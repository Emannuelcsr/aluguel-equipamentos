import { Component, OnInit } from '@angular/core';
import { EquipamentoResponse } from '../../../core/models/equipamento-response';
import { EquipamentoService } from '../service/equipamento.service';
import { AutenticacaoService } from '../../../core/services/autenticacao.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-listagem-equipamentos',
  imports: [RouterLink],
  templateUrl: './listagem-equipamentos.component.html',
  styleUrl: './listagem-equipamentos.component.scss',
})
export class ListagemEquipamentosComponent implements OnInit {
  equipamentos: EquipamentoResponse[] = [];
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
  ehAdministrador = false;

  constructor(
    private equipamentoService: EquipamentoService,
    private autenticacaoService: AutenticacaoService,
  ) {}

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

    this.equipamentoService.listar(pagina, this.tamanhoPagina).subscribe({
      next: (resposta) => {
        this.carregando = false;
        this.equipamentos = resposta.conteudo;
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
          erro.error?.mensagens?.[0] ??
          'Não foi possível listar os equipamentos.';
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
