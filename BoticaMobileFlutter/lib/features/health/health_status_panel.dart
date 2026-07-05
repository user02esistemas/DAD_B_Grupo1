import 'package:flutter/material.dart';

import '../../core/network/api_client.dart';
import 'health_service.dart';
import 'health_status.dart';

class HealthStatusPanel extends StatefulWidget {
  const HealthStatusPanel({super.key});

  @override
  State<HealthStatusPanel> createState() => _HealthStatusPanelState();
}

class _HealthStatusPanelState extends State<HealthStatusPanel> {
  late final HealthService _healthService;
  late Future<HealthStatus> _future;

  @override
  void initState() {
    super.initState();
    _healthService = HealthService(ApiClient());
    _future = _healthService.check();
  }

  void _reload() {
    setState(() => _future = _healthService.check());
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<HealthStatus>(
      future: _future,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const _HealthCard(
            icon: Icons.sync,
            color: Colors.blueGrey,
            title: 'Verificando API y RMI',
            subtitle: 'Consultando /api/health...',
          );
        }

        if (snapshot.hasError) {
          return _HealthCard(
            icon: Icons.cloud_off,
            color: Theme.of(context).colorScheme.error,
            title: 'API no disponible',
            subtitle: 'Revise Tomcat, RMI y base_url.',
            onRefresh: _reload,
          );
        }

        final health = snapshot.data!;
        if (health.isHealthy) {
          return _HealthCard(
            icon: Icons.verified,
            color: Colors.green,
            title: 'API y RMI disponibles',
            subtitle: health.message,
            onRefresh: _reload,
          );
        }

        final missing = health.missingServices.isEmpty ? '' : ' Faltan: ${health.missingServices.join(', ')}';
        return _HealthCard(
          icon: Icons.warning_amber_rounded,
          color: Colors.orange,
          title: 'Conexion parcial',
          subtitle: 'API: ${health.api} - RMI: ${health.rmi}.$missing',
          onRefresh: _reload,
        );
      },
    );
  }
}

class _HealthCard extends StatelessWidget {
  const _HealthCard({
    required this.icon,
    required this.color,
    required this.title,
    required this.subtitle,
    this.onRefresh,
  });

  final IconData icon;
  final Color color;
  final String title;
  final String subtitle;
  final VoidCallback? onRefresh;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            Icon(icon, color: color),
            const SizedBox(width: 10),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
                  const SizedBox(height: 2),
                  Text(subtitle, style: Theme.of(context).textTheme.bodySmall),
                ],
              ),
            ),
            if (onRefresh != null)
              IconButton(
                onPressed: onRefresh,
                icon: const Icon(Icons.refresh),
                tooltip: 'Verificar nuevamente',
              ),
          ],
        ),
      ),
    );
  }
}
