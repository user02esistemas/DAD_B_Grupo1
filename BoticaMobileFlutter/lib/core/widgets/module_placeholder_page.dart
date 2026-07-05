import 'package:flutter/material.dart';

class ModulePlaceholderPage extends StatelessWidget {
  const ModulePlaceholderPage({
    required this.title,
    required this.description,
    required this.nextSteps,
    super.key,
  });

  final String title;
  final String description;
  final List<String> nextSteps;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Icon(Icons.construction_rounded, size: 72, color: Theme.of(context).colorScheme.primary),
          const SizedBox(height: 16),
          Text(title, style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          Text(description),
          const SizedBox(height: 20),
          Text('Siguientes pasos', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          for (final step in nextSteps)
            Card(
              child: ListTile(
                leading: const Icon(Icons.check_circle_outline),
                title: Text(step),
              ),
            ),
        ],
      ),
    );
  }
}
