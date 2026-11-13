import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UnidadeEquipamentoService } from '../service/unidade-equipamento.service';
import { UnidadeEquipamentoResponse } from '../../../core/models/unidade-equipamento-response';
import { AutenticacaoService } from '../../../core/services/autenticacao.service';
import { CadastrarUnidadeEquipamentoRequest } from '../../../core/models/cadastrar-unidade-equipamento-request';
import { EquipamentoService } from '../../equipamento/service/equipamento.service';
import { EquipamentoResponse } from '../../../core/models/equipamento-response';

@Component({
  selector: 'app-listagem-unidades-equipamento',
  imports: [],
  templateUrl: './listagem-unidades-equipamento.component.html',
  styleUrl: './listagem-unidades-equipamento.component.scss',
})
export class ListagemUnidadesEquipamentoComponent implements OnInit {
  mensagemSucesso = '';
  mensagemErro = '';
  id!: number;
  paginaAtual = 1;
  tamanhoPagina = 5;
  totalPaginas = 0;
  primeiraPagina = true;
  ultimaPagina = false;
  totalElementos = 0;
  carregando = false;
  paginaSolicitada = 1;
  ehAdministrador = false;
  unidadeEquipamentos: UnidadeEquipamentoResponse[] = [];
  modalCadastroAberto = false;
  cadastrandoUnidade = false;
  equipamento: EquipamentoResponse | null = null;

  constructor(
    private route: ActivatedRoute,
    private unidadeEquipamentoService: UnidadeEquipamentoService,
    private autenticacaoService: AutenticacaoService,
    private equipamentoService: EquipamentoService,
  ) {}

  ngOnInit(): void {
    if (this.autenticacaoService.obterUsuario()?.perfil === 'ADMINISTRADOR') {
      this.ehAdministrador = true;
    } else {
      this.ehAdministrador = false;
    }

    const idParametro = this.route.snapshot.paramMap.get('id');

    if (!idParametro) {
      this.mensagemErro = 'ID do equipamento não informado.';
      return;
    }

    this.id = Number(idParametro);

    if (!Number.isInteger(this.id) || this.id <= 0) {
      this.mensagemErro = 'ID do equipamento inválido.';
      return;
    }

    this.listarPorEquipamento(this.paginaAtual);

    this.equipamentoService.buscarPorId(this.id).subscribe({
      next: (resposta) => {
        this.equipamento = resposta;
      },
      error: (erro) => {
        this.mensagemErro =
          erro.error?.mensagens?.[0] ??
          'Não foi possível carregar os dados do equipamento.';
      },
    });
  }

  abrirModalCadastro() {
    this.modalCadastroAberto = true;
  }

  fecharModalCadastro() {
    this.modalCadastroAberto = false;
  }

  listarPorEquipamento(pagina: number) {
    if (this.carregando) {
      return;
    }

    this.paginaSolicitada = pagina;
    this.carregando = true;
    this.mensagemErro = '';

    this.unidadeEquipamentoService
      .listarPorEquipamento(this.id, pagina, this.tamanhoPagina)
      .subscribe({
        next: (resposta) => {
          this.carregando = false;
          this.unidadeEquipamentos = resposta.conteudo;
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
            erro.error?.mensagens?.[0] ??
            'Não foi possível listar os equipamentos.';
        },
      });
  }

  proximaPagina() {
    if (!this.ultimaPagina) {
      this.listarPorEquipamento(this.paginaAtual + 1);
    }
  }

  voltarPagina() {
    if (this.paginaAtual > 1) {
      this.listarPorEquipamento(this.paginaAtual - 1);
    }
  }

  cadastrarUnidade() {
    if (this.cadastrandoUnidade) {
      return;
    }

    const dadosCadastro: CadastrarUnidadeEquipamentoRequest = {
      equipamentoId: this.id,
    };

    this.mensagemErro = '';
    this.cadastrandoUnidade = true;

    this.unidadeEquipamentoService.cadastrar(dadosCadastro).subscribe({
      next: (resposta) => {
        this.cadastrandoUnidade = false;
        this.fecharModalCadastro();

        this.mensagemSucesso = 'Unidade cadastrada com sucesso!';
        this.listarPorEquipamento(0);
      },
      error: (erro) => {
        this.cadastrandoUnidade = false;

        this.mensagemErro =
          erro.error?.mensagens?.[0] ?? 'Não foi possível cadastrar a unidade.';
      },
    });
  }
}
