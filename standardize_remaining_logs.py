import re
import os
from pathlib import Path

# Directorio base
base_dir = Path("src/main/java/com/harmony/sistema")

# Mapeo de emojis a niveles
emoji_to_level = {
    "🔵": "[INFO]",
    "✅": "[SUCCESS]",
    "❌": "[ERROR]",
    "⚠️": "[WARN]",
    "📱": "[INFO]",
    "📧": "[INFO]",
    "📝": "[INFO]",
    "📁": "[INFO]",
    "🔗": "[INFO]",
    "📞": "[INFO]",
    "🎓": "[INFO]",
    "🔑": "[INFO]",
    "👤": "[INFO]",
    "🚀": "[INFO]",
    "⚙️": "[INFO]",
    "🔐": "[INFO]",
    "📋": "[INFO]",
    "🔍": "[INFO]",
}

# Patrones de componentes antiguos a nuevos
component_patterns = {
    r"\[API ADMIN\]": "[CONTROLLER]",
    r"\[API ADMIN SUCCESS\]": "[CONTROLLER]",
    r"\[API ADMIN ERROR\]": "[CONTROLLER]",
    r"\[FILE UPLOAD\]": "[CONTROLLER]",
    r"\[FILE UPLOAD SUCCESS\]": "[CONTROLLER]",
    r"\[CONTACTO\]": "[CONTROLLER]",
    r"\[PROFESOR PUBLIC\]": "[CONTROLLER]",
}

def remove_emojis_and_standardize(line):
    """Elimina emojis y estandariza el formato de logs"""
    # Si no es una línea de log, retornarla sin cambios
    if "System.out.println" not in line and "System.err.println" not in line:
        return line
    
    modified = line
    
    # Reemplazar emojis por niveles
    for emoji, level in emoji_to_level.items():
        if emoji in modified:
            # Si el emoji está seguido de [API ADMIN SUCCESS] o similar, usar SUCCESS
            if "SUCCESS" in modified:
                modified = modified.replace(emoji, "[SUCCESS]", 1)
            elif "ERROR" in modified:
                modified = modified.replace(emoji, "[ERROR]", 1)
            else:
                modified = modified.replace(emoji, level, 1)
    
    # Reemplazar patrones de componentes
    for old_pattern, new_component in component_patterns.items():
        modified = re.sub(old_pattern, new_component, modified)
    
    # Cambiar System.out.println a System.err.println para errores
    if "[ERROR]" in modified and "System.out.println" in modified:
        modified = modified.replace("System.out.println", "System.err.println")
    
    return modified

def process_file(file_path):
    """Procesa un archivo Java y estandariza sus logs"""
    print(f"Procesando: {file_path}")
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        modified_lines = []
        changes_count = 0
        
        for line in lines:
            new_line = remove_emojis_and_standardize(line)
            if new_line != line:
                changes_count += 1
            modified_lines.append(new_line)
        
        if changes_count > 0:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.writelines(modified_lines)
            print(f"  ✓ {changes_count} líneas modificadas")
            return changes_count
        else:
            print(f"  - Sin cambios necesarios")
            return 0
            
    except Exception as e:
        print(f"  ✗ Error: {e}")
        return 0

def main():
    """Procesa todos los archivos pendientes"""
    files_to_process = [
        "controller/admin/AdminClienteController.java",
        "controller/admin/AdminHorarioController.java",
        "controller/admin/AdminTallerController.java",
        "controller/admin/AdminProfesorController.java",
        "controller/publico/ContactoController.java",
        "controller/publico/ProfesorPublicController.java",
        "config/DataInitializer.java",
    ]
    
    total_changes = 0
    
    for file_rel_path in files_to_process:
        file_path = base_dir / file_rel_path
        if file_path.exists():
            changes = process_file(file_path)
            total_changes += changes
        else:
            print(f"Archivo no encontrado: {file_path}")
    
    print(f"\n{'='*50}")
    print(f"Total de líneas modificadas: {total_changes}")
    print(f"{'='*50}")

if __name__ == "__main__":
    main()
