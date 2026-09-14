import os
import re

ctrl_dir = "/home/luka/Documentos/PROGRAMAS/Proyectos/SiGU/src/main/java/com/is1/proyecto/controllers"
pattern = re.compile(r'(return\s+new\s+ModelAndView\(\s*([a-zA-Z0-9_]+)\s*,\s*".*?"\s*\);)')

for file in os.listdir(ctrl_dir):
    if file.endswith(".java"):
        filepath = os.path.join(ctrl_dir, file)
        with open(filepath, 'r') as f:
            content = f.read()
        
        def replacer(match):
            full_match = match.group(1)
            var_name = match.group(2)
            injection = f"""if (req.session().attribute("loggedIn") != null && req.session().attribute("loggedIn").equals(true)) {{
    if (req.session().attribute("fotoPerfil") != null) {{
        {var_name}.put("foto_perfil", req.session().attribute("fotoPerfil"));
    }} else {{
        {var_name}.put("foto_perfil", "/img/default-avatar.png");
    }}
    if (!{var_name}.containsKey("username") && req.session().attribute("currentUserUsername") != null) {{
        {var_name}.put("username", req.session().attribute("currentUserUsername"));
    }}
}}
{full_match}"""
            # Avoid double injection
            if "req.session().attribute(\"fotoPerfil\")" in full_match:
                return full_match
            return injection

        # Before modifying, check if already injected
        if "req.session().attribute(\"fotoPerfil\")" not in content:
            new_content = pattern.sub(replacer, content)
            if new_content != content:
                with open(filepath, 'w') as f:
                    f.write(new_content)
                print(f"Modified {file}")

