import { Component, OnInit } from '@angular/core';
import { ClienteService } from '../service/cliente.service';
import { ClienteResponse } from '../../../core/models/cliente-response';
import { AutenticacaoService } from '../../../core/services/autenticacao.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-listagem-clientes',
  imports: [],
  templateUrl: './listagem-clientes.component.html',
  styleUrl: './listagem-clientes.component.scss',
})
export class ListagemClientesComponent implements OnInit {
  clientes: ClienteResponse[] = [];
  mensagemSucesso = '';
  mensagemErro = '';
  paginaAtual = 1;
  tamanhoPagina = 5;
  totalPaginas = 0;
  primeiraPagina = true;
  ultimaPagina = false;
  totalElementos = 0;
  carregando = false;

  constructor(private clienteService: ClienteService) {}
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
    this.carregando = true;
    this.mensagemErro = '';
    this.clienteService.listar(pagina, this.tamanhoPagina).subscribe({
      next: (resposta) => {
        this.carregando = false;
        this.clientes = resposta.conteudo;
        this.totalPaginas = resposta.totalPaginas;
        this.primeiraPagina = resposta.primeiraPagina;
        this.ultimaPagina = resposta.ultimaPagina;
        this.totalElementos = resposta.totalElementos;
        this.paginaAtual = resposta.paginaAtual;
      },
      error: () => {
        this.carregando = false;
        this.mensagemErro = 'Ocorreu um erro inesperado';
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
