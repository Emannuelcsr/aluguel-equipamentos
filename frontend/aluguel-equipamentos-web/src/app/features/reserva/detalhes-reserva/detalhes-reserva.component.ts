import { Component, OnInit } from '@angular/core';
import { ReservaService } from '../service/reserva.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ReservaResponse } from '../../../core/models/reserva-response';
import { AutenticacaoService } from '../../../core/services/autenticacao.service';
import { CurrencyPipe, DatePipe } from '@angular/common';

@Component({
  selector: 'app-detalhes-reserva',
  imports: [DatePipe,CurrencyPipe,RouterLink],
  templateUrl: './detalhes-reserva.component.html',
  styleUrl: './detalhes-reserva.component.scss'
})
export class DetalhesReservaComponent  implements OnInit{
  
    reserva!: ReservaResponse;
    mensagemErro = '';
    carregando = false;
    id!: number;
    rejeitando = false;
    aprovando = false;
    cancelando = false;
    mensagemSucesso = '';
    ehAdministrador=false;

    constructor(  private reservaService: ReservaService,
      private autenticacaoService: AutenticacaoService,
    private route: ActivatedRoute,){}
  
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
        this.mensagemErro = 'ID da reserva inválido.';
        return;
      }

      this.carregando = true;
      this.mensagemErro = '';
      this.reservaService.buscarPorId(this.id).subscribe({
        next: (resposta) => {
          this.mensagemErro = '';
          this.carregando = false;
          this.reserva = resposta;
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
            'Não foi possível carregar os dados da reserva.';
        },
      });
    } else {
      this.mensagemErro = 'ID da reserva não informada.';
    }
  }


    aprovarReserva() {
    if (this.aprovando || this.rejeitando  || this.cancelando) {
      return;
    }

    const confirmou = confirm('Deseja realmente aprovar esta reserva?');

    if (!confirmou) {
      return;
    }
    this.aprovando = true;
    this.mensagemErro = '';
    this.mensagemSucesso = '';
    this.reservaService.aprovar(this.id).subscribe({
      next: (resposta) => {
        this.mensagemErro = '';
        this.mensagemSucesso = 'Reserva aprovada com sucesso!';
        this.aprovando = false;
        this.reserva.status = resposta.status;
      },
      error: (erro) => {
        this.aprovando = false;

        this.mensagemSucesso = '';
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ??
          'Não foi possível aprovar a reserva.';
      },
    });
  }

      rejeitarReserva() {
    if (this.aprovando || this.rejeitando  || this.cancelando) {
      return;
    }

    const confirmou = confirm('Deseja realmente rejeitar esta reserva?');

    if (!confirmou) {
      return;
    }
    this.rejeitando = true;
    this.mensagemErro = '';
    this.mensagemSucesso = '';
    this.reservaService.rejeitar(this.id).subscribe({
      next: (resposta) => {
        this.mensagemErro = '';
        this.mensagemSucesso = 'Reserva rejeitada com sucesso!';
        this.rejeitando = false;
        this.reserva.status = resposta.status;
      },
      error: (erro) => {
        this.rejeitando = false;

        this.mensagemSucesso = '';
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ??
          'Não foi possível rejeitar a reserva.';
      },
    });
  }

    cancelarReserva() {
    if (this.aprovando || this.rejeitando  || this.cancelando) {
      return;
    }

    const confirmou = confirm('Deseja realmente cancelar esta reserva?');

    if (!confirmou) {
      return;
    }
    this.cancelando = true;
    this.mensagemErro = '';
    this.mensagemSucesso = '';
    this.reservaService.cancelar(this.id).subscribe({
      next: (resposta) => {
        this.mensagemErro = '';
        this.mensagemSucesso = 'Reserva cancelada com sucesso!';
        this.cancelando = false;
        this.reserva.status = resposta.status;
      },
      error: (erro) => {
        this.cancelando = false;

        this.mensagemSucesso = '';
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ??
          'Não foi possível cancelar a reserva.';
      },
    });
  }

}
