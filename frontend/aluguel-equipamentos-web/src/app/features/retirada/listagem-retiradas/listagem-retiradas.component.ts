import { Component, OnInit } from '@angular/core';
import { RetiradaResponse } from '../../../core/models/retirada-response';
import { RetiradaService } from '../service/retirada.service';
import { RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ReservaService } from '../../reserva/service/reserva.service';
import { ReservaResponse } from '../../../core/models/reserva-response';

@Component({
  selector: 'app-listagem-retiradas',
  imports: [RouterLink,CurrencyPipe,DatePipe],
  templateUrl: './listagem-retiradas.component.html',
  styleUrl: './listagem-retiradas.component.scss',
})
export class ListagemRetiradasComponent implements OnInit {
  retiradas: RetiradaResponse[] = [];
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

  mensagemSucessoAprovada = '';
  mensagemErroAprovada = '';
  paginaAtualAprovada = 1;
  tamanhoPaginaAprovada = 5;
  totalPaginasAprovada = 0;
  primeiraPaginaAprovada = true;
  ultimaPaginaAprovada = false;
  totalElementosAprovada = 0;
  carregandoAprovada = false;
  paginaSolicitadaAprovada = 1;
  reservaAprovada: ReservaResponse[] = [];
  


  constructor(private retiradaService: RetiradaService, private reservaService: ReservaService) {}

  ngOnInit(): void {
    this.mensagemSucessoAprovada = history.state.mensagemSucessoAprovada ?? '';

    if (this.mensagemSucessoAprovada) {
      const estadoAtual = { ...history.state };
      delete estadoAtual.mensagemSucessoAprovada;

      history.replaceState(estadoAtual, '');
    }

    this.listar(this.paginaAtual);
    this.listarAprovadas(this.paginaAtualAprovada);
  }

  listar(pagina: number) {
    if (this.carregando) {
      return;
    }
    this.paginaSolicitada = pagina;
    this.carregando = true;
    this.mensagemErro = '';

    this.retiradaService.listar(pagina, this.tamanhoPagina).subscribe({
      next: (resposta) => {
        this.carregando = false;
        this.retiradas = resposta.conteudo;
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
          erro.error?.mensagens?.[0] ?? 'Não foi possível listar as retiradas.';
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


  listarAprovadas(paginaAprovada: number) {
    if (this.carregandoAprovada) {
      return;
    }
    this.paginaSolicitadaAprovada = paginaAprovada;
    this.carregandoAprovada = true;
    this.mensagemErroAprovada = '';

    this.reservaService.listarAprovadas(paginaAprovada, this.tamanhoPaginaAprovada).subscribe({
      next: (resposta) => {
        console.log(resposta)
        this.carregandoAprovada = false;
        this.reservaAprovada = resposta.conteudo;
        this.totalPaginasAprovada = resposta.totalPaginas;
        this.primeiraPaginaAprovada = resposta.primeiraPagina;
        this.ultimaPaginaAprovada = resposta.ultimaPagina;
        this.totalElementosAprovada = resposta.totalElementos;
        this.paginaAtualAprovada = resposta.paginaAtual;
      },
      error: (erro) => {
        this.carregandoAprovada = false;
        if (erro.status === 0) {
          this.mensagemErroAprovada =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErroAprovada =
          erro.error?.mensagens?.[0] ?? 'Não foi possível listar as reservas aprovadas.';
      },
    });
  }

proximaPaginaAprovada() {
  if (!this.ultimaPaginaAprovada) {
    this.listarAprovadas(this.paginaAtualAprovada + 1);
  }
}

voltarPaginaAprovada() {
  if (this.paginaAtualAprovada > 1) {
    this.listarAprovadas(this.paginaAtualAprovada - 1);
  }
}

}
