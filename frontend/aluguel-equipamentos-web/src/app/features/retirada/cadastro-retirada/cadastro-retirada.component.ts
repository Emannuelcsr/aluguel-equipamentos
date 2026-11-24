import { Component } from '@angular/core';
import {
  FormArray,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MetodoVerificacaoRetirada } from '../../../core/models/metodo-verificacao-retirada.enum';
import { CondicaoFisicaRetirada } from '../../../core/models/condicao-fisica-enum';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReservaService } from '../../reserva/service/reserva.service';
import { ReservaResponse } from '../../../core/models/reserva-response';
import { RetiradaService } from '../service/retirada.service';
import { CadastrarRetiradaRequest } from '../../../core/models/cadastrar-retirada-request';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-cadastro-retirada',
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './cadastro-retirada.component.html',
  styleUrl: './cadastro-retirada.component.scss',
})
export class CadastroRetiradaComponent {
  mensagemErro = '';
  reserva: ReservaResponse | null = null;
  cadastrando = false;
  mensagemSucesso = '';

  constructor(
    private route: ActivatedRoute,
    private reservaService: ReservaService,
    private retiradaService: RetiradaService,
    private router: Router,
  ) {}

  formularioCadastro = new FormGroup({
    documentoRecebedor: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d{11}$/)],
    }),
    nomeRecebedor: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(150)],
    }),
    metodoVerificacao: new FormControl<MetodoVerificacaoRetirada | null>(null, {
      validators: [Validators.required],
    }),
    descricaoOutroMetodo: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(500)],
    }),
    autorizacaoConfirmada: new FormControl<boolean>(false, {
      nonNullable: true,
      validators: [Validators.requiredTrue],
    }),
    recebimentoConfirmado: new FormControl<boolean>(false, {
      nonNullable: true,
      validators: [Validators.requiredTrue],
    }),

    itens: new FormArray<
      ReturnType<CadastroRetiradaComponent['criarFormularioItem']>
    >([]),
  });

  criarFormularioItem(itemReservaId: number) {
    const grupo = new FormGroup({
      itemReservaId: new FormControl(itemReservaId, {
        nonNullable: true,
      }),

      condicaoFisica: new FormControl<CondicaoFisicaRetirada | null>(null, {
        validators: [Validators.required],
      }),
      observacoes: new FormControl('', {
        nonNullable: true,
        validators: [Validators.maxLength(500)],
      }),
    });

    grupo.controls.condicaoFisica.valueChanges.subscribe((condicao) => {
      if (condicao === CondicaoFisicaRetirada.COM_OBSERVACOES) {
        grupo.controls.observacoes.setValidators([
          Validators.required,
          Validators.maxLength(500),
        ]);
      } else {
        grupo.controls.observacoes.setValidators([Validators.maxLength(500)]);
      }
      grupo.controls.observacoes.updateValueAndValidity();
    });

    return grupo;
  }

  adicionarItem(itemReservaId: number) {
    this.formularioCadastro.controls.itens.push(
      this.criarFormularioItem(itemReservaId),
    );
  }

  ngOnInit(): void {
    const reservaId = this.route.snapshot.paramMap.get('reservaId');

    const id = Number(reservaId);

    if (!reservaId || !Number.isInteger(id) || id <= 0) {
      this.mensagemErro = 'ID da reserva inválido.';
      return;
    }

    this.reservaService.buscarPorId(id).subscribe({
      next: (resposta) => {
        this.reserva = resposta;

        resposta.itens.forEach((item) => {
          this.adicionarItem(item.id);
        });
      },
      error: (erro) => {
        this.mensagemErro = 'Não foi possível carregar a reserva.';
      },
    });

    this.formularioCadastro.controls.metodoVerificacao.valueChanges.subscribe(
      (metodoVerificacao) => {
        if (metodoVerificacao === MetodoVerificacaoRetirada.OUTRO) {
          this.formularioCadastro.controls.descricaoOutroMetodo.setValidators([
            Validators.required,
            Validators.maxLength(500),
          ]);
        } else {
          this.formularioCadastro.controls.descricaoOutroMetodo.setValidators([
            Validators.maxLength(500),
          ]);
        }
        this.formularioCadastro.controls.descricaoOutroMetodo.updateValueAndValidity();
      },
    );
  }

  registrarRetirada(): void {
    if (this.cadastrando || this.mensagemSucesso) {
      return;
    }

    this.mensagemErro = '';

    if (this.formularioCadastro.invalid) {
      this.formularioCadastro.markAllAsTouched();
      this.mensagemErro = 'Preencha corretamente os campos obrigatórios.';
      return;
    }

    const dadosFormulario = this.formularioCadastro.getRawValue();

    if (dadosFormulario.metodoVerificacao === null) {
      return;
    }

    if (dadosFormulario.itens.some((item) => item.condicaoFisica === null)) {
      return;
    }

    if (!this.reserva) {
      return;
    }

    const reservaId = this.reserva.id;

    const dadosRetirada: CadastrarRetiradaRequest = {
      ...dadosFormulario,
      reservaId: reservaId,
      metodoVerificacao: dadosFormulario.metodoVerificacao,
      itens: dadosFormulario.itens
        .filter((item) => item.condicaoFisica !== null)
        .map((item) => ({
          itemReservaId: item.itemReservaId,
          condicaoFisica: item.condicaoFisica!,
          observacoes: item.observacoes,
        })),
    };

    this.cadastrando = true;
    this.retiradaService.cadastrar(dadosRetirada).subscribe({
      next: (resposta) => {
        this.cadastrando = false;
        console.log('Retirada cadastrada:', resposta);
        this.mensagemSucesso = 'Retirada registrada com sucesso!';
      },
      error: (erro) => {
        this.cadastrando = false;
        this.mensagemErro =
          erro.error?.mensagens?.[0] ??
          'Não foi possível registrar a retirada.';
      },
    });
  }

  confirmarSaida(): boolean {
    if (this.formularioCadastro.dirty && !this.mensagemSucesso) {
      return confirm('Existem dados não salvos. Deseja realmente sair?');
    }

    return true;
  }

  voltar(): void {
    if (!this.confirmarSaida()) {
      return;
    }

    if (this.reserva) {
      this.router.navigate(['/reservas/detalhes', this.reserva.id]);
    }
  }
}
