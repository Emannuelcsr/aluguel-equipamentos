import { Component, OnInit } from '@angular/core';
import { DevolucaoService } from '../service/devolucao.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DevolucaoResponse } from '../../../core/models/devolucao-response';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-detalhes-devolucao',
  imports: [DatePipe, RouterLink],
  templateUrl: './detalhes-devolucao.component.html',
  styleUrl: './detalhes-devolucao.component.scss',
})
export class DetalhesDevolucaoComponent implements OnInit {
  devolucao!: DevolucaoResponse;
  mensagemErro = '';
  carregando = false;
  id!: number;

  constructor(
    private devolucaoService: DevolucaoService,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    const idParametro = this.route.snapshot.paramMap.get('id');

    if (!idParametro) {
      this.mensagemErro = 'ID da devolução não informado.';
      return;
    }

    this.id = Number(idParametro);

    if (!Number.isInteger(this.id) || this.id <= 0) {
      this.mensagemErro = 'ID da devolução inválido.';
      return;
    }

    this.carregando = true;
    this.mensagemErro = '';

    this.devolucaoService.buscarPorId(this.id).subscribe({
      next: (resposta) => {
        this.carregando = false;
        this.mensagemErro = '';
        this.devolucao = resposta;
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
          'Não foi possível carregar os dados da devolução.';
      },
    });
  }
}
