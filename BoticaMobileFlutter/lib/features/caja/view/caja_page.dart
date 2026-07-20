import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../../core/widgets/error_panel.dart';
import '../../../core/widgets/mobile_module_widgets.dart';
import '../../auth/model/user.dart';
import '../service/cash_service.dart';

class CajaPage extends StatefulWidget {
  const CajaPage({required this.user, super.key});

  final User user;

  @override
  State<CajaPage> createState() => _CajaPageState();
}

class _CajaPageState extends State<CajaPage> {
  final _amountController = TextEditingController(text: '0.00');
  late final CashService _service;
  CashSession? _session;
  String? _error;
  bool _loading = true;
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _service = CashService(ApiClient());
    _load();
  }

  @override
  void dispose() {
    _amountController.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final session = await _service.activeSession(widget.user.id);
      if (!mounted) return;
      setState(() => _session = session);
    } catch (ex) {
      if (!mounted) return;
      setState(() => _error = ex.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _submit() async {
    final amount = double.tryParse(_amountController.text.trim()) ?? 0;
    setState(() => _saving = true);
    try {
      if (_session == null) {
        await _service.openSession(userId: widget.user.id, amount: amount);
        _showMessage('Caja abierta correctamente.');
      } else {
        await _service.closeSession(userId: widget.user.id, amount: amount);
        _showMessage('Caja cerrada correctamente.');
      }
      await _load();
    } catch (ex) {
      if (!mounted) return;
      _showMessage(ex.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  void _showMessage(String message) {
    ScaffoldMessenger.of(context)
        .showSnackBar(SnackBar(content: Text(message)));
  }

  @override
  Widget build(BuildContext context) {
    const color = Color(0xFFE67700);
    final session = _session;
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
            icon: const Icon(Icons.arrow_back_rounded),
            onPressed: () => Navigator.of(context).maybePop()),
        title: const ModuleAppTitle(
            title: 'Caja', icon: Icons.account_balance_wallet_rounded),
        actions: [
          IconButton(onPressed: _load, icon: const Icon(Icons.refresh_rounded))
        ],
      ),
      bottomNavigationBar: AppQuickNavBar(
        current: 'caja',
        onHome: () => Navigator.of(context).popUntil((route) => route.isFirst),
        onAlerts: () => _showMessage('Revisa las alertas desde Inicio.'),
        onCaja: _load,
        onProfile: () => showQuickProfileSheet(
          context,
          name: widget.user.nombreCompleto,
          username: widget.user.username,
          role: widget.user.rolesLabel,
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 104),
        children: [
          if (_error != null) ErrorPanel(message: _error!, onRetry: _load),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(children: [
                      CircleAvatar(
                          backgroundColor: color.withValues(alpha: 0.12),
                          child: Icon(
                              session == null
                                  ? Icons.lock_rounded
                                  : Icons.lock_open_rounded,
                              color: color)),
                      const SizedBox(width: 12),
                      Expanded(
                          child: Text(
                              session == null ? 'Caja cerrada' : 'Caja abierta',
                              style: Theme.of(context)
                                  .textTheme
                                  .titleLarge
                                  ?.copyWith(fontWeight: FontWeight.w900))),
                    ]),
                    const SizedBox(height: 12),
                    Text(session == null
                        ? 'Abra caja para habilitar ventas del turno.'
                        : '${session.cajaNombre} - efectivo esperado S/ ${session.efectivoEsperado.toStringAsFixed(2)}'),
                    if (session != null) ...[
                      const SizedBox(height: 12),
                      Wrap(spacing: 8, runSpacing: 8, children: [
                        _Chip(
                            'Inicial S/ ${session.montoInicial.toStringAsFixed(2)}'),
                        _Chip(
                            'Efectivo S/ ${session.totalVentasEfectivo.toStringAsFixed(2)}'),
                        _Chip(
                            'Virtual S/ ${session.totalVentasVirtual.toStringAsFixed(2)}'),
                      ]),
                    ],
                  ]),
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(session == null ? 'Abrir caja' : 'Cerrar caja',
                        style: Theme.of(context)
                            .textTheme
                            .titleLarge
                            ?.copyWith(fontWeight: FontWeight.w900)),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _amountController,
                      keyboardType:
                          const TextInputType.numberWithOptions(decimal: true),
                      decoration: InputDecoration(
                        labelText: session == null
                            ? 'Monto inicial'
                            : 'Monto final contado',
                        prefixText: 'S/ ',
                      ),
                    ),
                    const SizedBox(height: 14),
                    FilledButton.icon(
                      onPressed: _loading || _saving ? null : _submit,
                      icon: _saving
                          ? const SizedBox(
                              width: 18,
                              height: 18,
                              child: CircularProgressIndicator(strokeWidth: 2))
                          : Icon(session == null
                              ? Icons.lock_open_rounded
                              : Icons.lock_rounded),
                      label:
                          Text(session == null ? 'Abrir caja' : 'Cerrar caja'),
                    ),
                  ]),
            ),
          ),
        ],
      ),
    );
  }
}

class _Chip extends StatelessWidget {
  const _Chip(this.label);
  final String label;

  @override
  Widget build(BuildContext context) => Chip(label: Text(label));
}
