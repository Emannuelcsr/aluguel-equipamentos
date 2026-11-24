import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { RetiradaResponse } from '../../../core/models/retirada-response';
import { RetiradaService } from '../service/retirada.service';

@Component({
  selector: 'app-detalhes-retirada',
  imports: [DatePipe, RouterLink],
  templateUrl: './detalhes-retirada.component.html',
  styleUrl: './detalhes-retirada.component.scss',
})
export class DetalhesRetiradaComponent implements OnInit {
  retirada!: RetiradaResponse;
  mensagemErro = '';
  carregando = false;

  id!: number;
  constructor(
    private retiradaService: RetiradaService,
       private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    const idParametro = this.route.snapshot.paramMap.get('id');

    if (idParametro) {
      this.id = Number(idParametro);

      if (!Number.isInteger(this.id) || this.id <= 0) {
        this.mensagemErro = 'ID da retirada inválido.';
        return;
      }
      this.carregando = true;
      this.mensagemErro = '';
      this.retiradaService.buscarPorId(this.id).subscribe({
        next: (resposta) => {
          this.mensagemErro = '';
          this.carregando = false;
          this.retirada = resposta;
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
            'Não foi possível carregar os dados da retirada.';
        },
      });
    } else {
      this.mensagemErro = 'ID da retirada não informada.';
    }
  }
}
