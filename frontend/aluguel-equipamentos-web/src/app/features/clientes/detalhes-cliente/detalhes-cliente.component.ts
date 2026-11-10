import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ClienteService } from '../service/cliente.service';
import { ClienteResponse } from '../../../core/models/cliente-response';

@Component({
  selector: 'app-detalhes-cliente',
  imports: [RouterLink],
  templateUrl: './detalhes-cliente.component.html',
  styleUrl: './detalhes-cliente.component.scss',
})
export class DetalhesClienteComponent implements OnInit {
  cliente!: ClienteResponse;
  mensagemErro = '';
  carregando = false;
  id!: number;
  desativando = false;
  ativando = false;

  constructor(
    private clienteService: ClienteService,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    const idParametro = this.route.snapshot.paramMap.get('id');

    if (idParametro) {
      this.id = Number(idParametro);

      if (!Number.isInteger(this.id) || this.id <= 0) {
        this.mensagemErro = 'ID do cliente inválido.';
        return;
      }

      this.carregando = true;
      this.clienteService.buscarPorId(this.id).subscribe({
        next: (resposta) => {
          this.carregando = false;
          this.cliente = resposta;
        },
        error: (erro) => {
          this.carregando = false;
          this.mensagemErro =
            erro.error?.mensagens?.[0] ?? 'Ocorreu um erro inesperado';
        },
      });
    } else {
      this.mensagemErro = 'ID do cliente não informado.';
    }
  }

  desativarCliente() {
    if (this.desativando) {
      return;
    }

    const confirmou = confirm('Deseja realmente desativar este cliente?');

    if (!confirmou) {
      return;
    }

    this.desativando = true;
    this.clienteService.desativarCliente(this.id).subscribe({
      next: (resposta) => {
        this.desativando = false;
        this.cliente.ativo = resposta.ativo;
      },
      error: (erro) => {
        this.desativando = false;
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ?? 'Não foi possível desativar o cliente.';
      },
    });
  }

  ativarCliente() {
    if (this.ativando) {
      return;
    }

    const confirmou = confirm('Deseja realmente ativar este cliente?');

    if (!confirmou) {
      return;
    }
    this.ativando = true;
    this.clienteService.ativarCliente(this.id).subscribe({
      next: (resposta) => {
        this.ativando = false;
        this.cliente.ativo = resposta.ativo;
      },
      error: (erro) => {
        this.ativando = false;
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ?? 'Não foi possível ativar o cliente.';
      },
    });
  }
}
