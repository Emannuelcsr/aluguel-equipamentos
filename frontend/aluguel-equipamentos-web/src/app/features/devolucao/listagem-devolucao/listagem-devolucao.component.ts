import { Component, OnInit } from '@angular/core';
import { DevolucaoResponse } from '../../../core/models/devolucao-response';
import { DevolucaoService } from '../service/devolucao.service';
import { ReservaService } from '../../reserva/service/reserva.service';
import { ReservaResponse } from '../../../core/models/reserva-response';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-listagem-devolucao',
  imports: [RouterLink,CurrencyPipe,DatePipe],
  templateUrl: './listagem-devolucao.component.html',
  styleUrl: './listagem-devolucao.component.scss',
})
export class ListagemDevolucaoComponent implements OnInit {
  devolucoes: DevolucaoResponse[] = [];
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

  mensagemSucessoAlugada = '';
  mensagemErroAlugada = '';
  paginaAtualAlugada = 1;
  tamanhoPaginaAlugada = 5;
  totalPaginasAlugada = 0;
  primeiraPaginaAlugada = true;
  ultimaPaginaAlugada = false;
  totalElementosAlugada = 0;
  carregandoAlugada = false;
  paginaSolicitadaAlugada = 1;
  reservasAlugadas: ReservaResponse[] = [];

  constructor(
    private devolucaoService: DevolucaoService,
    private reservaService: ReservaService,
  ) {}

  ngOnInit(): void {
    
    this.mensagemSucessoAlugada = history.state.mensagemSucessoAlugada ?? '';

    if (this.mensagemSucessoAlugada) {
      const estadoAtual = { ...history.state };
      delete estadoAtual.mensagemSucessoAlugada;

      history.replaceState(estadoAtual, '');
    }

    this.listar(this.paginaAtual);
    this.listarAlugadas(this.paginaAtualAlugada);
  }

  listar(pagina: number) {
    if (this.carregando) {
      return;
    }
    this.paginaSolicitada = pagina;
    this.carregando = true;
    this.mensagemErro = '';

    this.devolucaoService.listar(pagina, this.tamanhoPagina).subscribe({
      next: (resposta) => {
        this.carregando = false;
        this.devolucoes = resposta.conteudo;
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
          'Não foi possível listar as devolucoes.';
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

  listarAlugadas(paginaAlugada: number) {
    if (this.carregandoAlugada) {
      return;
    }
    this.paginaSolicitadaAlugada = paginaAlugada;
    this.carregandoAlugada = true;
    this.mensagemErroAlugada = '';

    this.reservaService
      .listarAlugadas(paginaAlugada, this.tamanhoPaginaAlugada)
      .subscribe({
        next: (resposta) => {
          console.log(resposta);
          this.carregandoAlugada = false;
          this.reservasAlugadas = resposta.conteudo;
          this.totalPaginasAlugada = resposta.totalPaginas;
          this.primeiraPaginaAlugada = resposta.primeiraPagina;
          this.ultimaPaginaAlugada = resposta.ultimaPagina;
          this.totalElementosAlugada = resposta.totalElementos;
          this.paginaAtualAlugada = resposta.paginaAtual;
        },
        error: (erro) => {
          this.carregandoAlugada = false;
          if (erro.status === 0) {
            this.mensagemErroAlugada =
              'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
            return;
          }

          this.mensagemErroAlugada =
            erro.error?.mensagens?.[0] ??
            'Não foi possível listar as reservas Alugadas.';
        },
      });
  }

  proximaPaginaAlugada() {
    if (!this.ultimaPaginaAlugada) {
      this.listarAlugadas(this.paginaAtualAlugada + 1);
    }
  }

  voltarPaginaAlugada() {
    if (this.paginaAtualAlugada > 1) {
      this.listarAlugadas(this.paginaAtualAlugada - 1);
    }
  }
}
