import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
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
  mensagemSucesso = '';
  mensagemErro = '';
  carregando = false;
  id!: number;

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
}
