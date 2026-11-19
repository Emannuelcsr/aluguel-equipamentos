import { Component, OnInit } from '@angular/core';
import { ReservaResponse } from '../../../core/models/reserva-response';
import { ReservaService } from '../service/reserva.service';
import { RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';

@Component({
  selector: 'app-listagem-reserva',
  imports: [RouterLink,CurrencyPipe,DatePipe],
  templateUrl: './listagem-reserva.component.html',
  styleUrl: './listagem-reserva.component.scss'
})
export class ListagemReservaComponent implements OnInit {
    reservas: ReservaResponse[] = [];
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

    constructor(private reservaService: ReservaService) {}
  ngOnInit(): void {

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

    this.reservaService.listar(pagina, this.tamanhoPagina).subscribe({
      next: (resposta) => {
        this.carregando = false;
        this.reservas = resposta.conteudo;
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
          erro.error?.mensagens?.[0] ?? 'Não foi possível listar as reservas.';
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
