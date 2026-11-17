import { Component, OnInit } from '@angular/core';
import { AutenticacaoService } from '../../../core/services/autenticacao.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { UnidadeEquipamentoService } from '../service/unidade-equipamento.service';
import { UnidadeEquipamentoResponse } from '../../../core/models/unidade-equipamento-response';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-detalhes-unidade-equipamento',
  imports: [DatePipe, RouterLink],
  templateUrl: './detalhes-unidade-equipamento.component.html',
  styleUrl: './detalhes-unidade-equipamento.component.scss',
})
export class DetalhesUnidadeEquipamentoComponent implements OnInit {
  unidadeEquipamento!: UnidadeEquipamentoResponse;
  mensagemSucesso = '';
  mensagemErro = '';
  id!: number;
  carregando = false;
  ehAdministrador = false;
  desativando = false;
  ativando = false;
  manutencao = false;

  constructor(
    private route: ActivatedRoute,
    private unidadeEquipamentoService: UnidadeEquipamentoService,
    private autenticacaoService: AutenticacaoService,
  ) {}

  ngOnInit(): void {
    if (this.autenticacaoService.obterUsuario()?.perfil == 'ADMINISTRADOR') {
      this.ehAdministrador = true;
    } else {
      this.ehAdministrador = false;
    }

    const idParametro = this.route.snapshot.paramMap.get('id');

    if (!idParametro) {
      this.mensagemErro = 'ID da unidade de equipamento não informado.';
      return;
    }

    this.id = Number(idParametro);

    if (!Number.isInteger(this.id) || this.id <= 0) {
      this.mensagemErro = 'ID da unidade de equipamento inválido.';
      return;
    }

    this.mensagemErro = '';
    this.carregando = true;
    this.unidadeEquipamentoService.buscarPorId(this.id).subscribe({
      next: (resposta) => {
        this.mensagemErro = '';
        this.carregando = false;
        this.unidadeEquipamento = resposta;
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
          'Não foi possível carregar os dados da unidade de equipamento.';
      },
    });
  }

  desativarunidadeEquipamento() {
    if (this.desativando) {
      return;
    }

    const confirmou = confirm('Deseja realmente desativar este equipamento?');

    if (!confirmou) {
      return;
    }

    this.desativando = true;
    this.mensagemErro = '';
    this.mensagemSucesso = '';
    this.unidadeEquipamentoService
      .desativarUnidadeEquipamento(this.id)
      .subscribe({
        next: (resposta) => {
          this.mensagemErro = '';
          this.mensagemSucesso =
            'Unidade de equipamento desativado com sucesso!';
          this.desativando = false;
          this.unidadeEquipamento.ativo = resposta.ativo;
        },
        error: (erro) => {
          this.desativando = false;
          this.mensagemSucesso = '';
          if (erro.status === 0) {
            this.mensagemErro =
              'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
            return;
          }

          this.mensagemErro =
            erro.error?.mensagens?.[0] ??
            'Não foi possível desativar o equipamento.';
        },
      });
  }

  ativarunidadeEquipamento() {
    if (this.ativando) {
      return;
    }

    const confirmou = confirm('Deseja realmente ativar este equipamento?');

    if (!confirmou) {
      return;
    }
    this.ativando = true;
    this.mensagemErro = '';
    this.mensagemSucesso = '';
    this.unidadeEquipamentoService.ativarUnidadeEquipamento(this.id).subscribe({
      next: (resposta) => {
        this.mensagemErro = '';
        this.mensagemSucesso = 'Equipamento ativado com sucesso!';
        this.manutencao = false;
        this.unidadeEquipamento.ativo = resposta.ativo;
      },
      error: (erro) => {
        this.ativando = false;

        this.mensagemSucesso = '';
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ??
          'Não foi possível ativar o equipamento.';
      },
    });
  }

  enviarManutencao(id: number) {
    if (this.manutencao) {
      return;
    }

    const confirmou = confirm(
      'Deseja realmente enviar este equipamento para manutenção?',
    );

    if (!confirmou) {
      return;
    }

    this.manutencao = true;
    this.mensagemSucesso = '';
    this.mensagemErro = '';
    this.unidadeEquipamentoService
      .enviarUnidadeEquipamentoManutencao(id)
      .subscribe({
        next: (resposta) => {
          this.mensagemErro = '';
          this.mensagemSucesso = 'Unidade enviada para manutenção com sucesso!';
          this.manutencao = false;
          this.unidadeEquipamento.status = resposta.status;
        },
        error: (erro) => {
          this.manutencao = false;
          this.mensagemSucesso = '';
          if (erro.status === 0) {
            this.mensagemErro =
              'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
            return;
          }

          this.mensagemErro =
            erro.error?.mensagens?.[0] ??
            'Não foi possível enviar a unidade para manutenção.';
        },
      });
  }

  liberarManutencao(id: number) {
    if (this.manutencao) {
      return;
    }
    const confirmou = confirm(
      'Deseja realmente liberar este equipamento da manutenção?',
    );

    if (!confirmou) {
      return;
    }
    this.mensagemSucesso = '';
    this.mensagemErro = '';
    this.manutencao = true;
    this.unidadeEquipamentoService
      .liberarUnidadeEquipamentoDaManutencao(id)
      .subscribe({
        next: (resposta) => {
          this.mensagemErro = '';
          this.mensagemSucesso = 'Unidade liberada da manutenção com sucesso!';
          this.manutencao = false;
          this.unidadeEquipamento.status = resposta.status;
        },
        error: (erro) => {
          this.manutencao = false;
          this.mensagemSucesso = '';
          if (erro.status === 0) {
            this.mensagemErro =
              'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
            return;
          }

          this.mensagemErro =
            erro.error?.mensagens?.[0] ??
            '	Não foi possível liberar a unidade da manutenção.';
        },
      });
  }
}
