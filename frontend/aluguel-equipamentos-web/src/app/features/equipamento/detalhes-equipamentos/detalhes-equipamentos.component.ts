import { Component, OnInit } from '@angular/core';
import { EquipamentoResponse } from '../../../core/models/equipamento-response';
import { EquipamentoService } from '../service/equipamento.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AutenticacaoService } from '../../../core/services/autenticacao.service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-detalhes-equipamentos',
  imports: [RouterLink, DatePipe],
  templateUrl: './detalhes-equipamentos.component.html',
  styleUrl: './detalhes-equipamentos.component.scss',
})
export class DetalhesEquipamentosComponent implements OnInit {
  equipamento!: EquipamentoResponse;
  mensagemErro = '';
  carregando = false;
  id!: number;
  desativando = false;
  ativando = false;
  ehAdministrador = false;
  mensagemSucesso = '';

  constructor(
    private equipamentoService: EquipamentoService,
    private route: ActivatedRoute,
    private autenticacaoService: AutenticacaoService,
  ) {}

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
        this.mensagemErro = 'ID do equipamento inválido.';
        return;
      }

      this.carregando = true;
      this.mensagemErro = '';
      this.equipamentoService.buscarPorId(this.id).subscribe({
        next: (resposta) => {
          this.mensagemErro = '';
          this.carregando = false;
          this.equipamento = resposta;
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
            'Não foi possível carregar os dados do equipamento.';
        },
      });
    } else {
      this.mensagemErro = 'ID do equipamento não informado.';
    }
  }

  desativarEquipamento() {
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
    this.equipamentoService.desativarEquipamento(this.id).subscribe({
      next: (resposta) => {
        this.mensagemErro = '';
        this.mensagemSucesso = 'Equipamento desativado com sucesso!';
        this.desativando = false;
        this.equipamento.ativo = resposta.ativo;
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

  ativarEquipamento() {
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
    this.equipamentoService.ativarEquipamento(this.id).subscribe({
      next: (resposta) => {
        this.mensagemErro = '';
        this.mensagemSucesso = 'Equipamento ativado com sucesso!';
        this.ativando = false;
        this.equipamento.ativo = resposta.ativo;
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
}
