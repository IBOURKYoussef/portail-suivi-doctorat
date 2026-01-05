-- Créer les bases de données
CREATE DATABASE IF NOT EXISTS doctorat_users;
CREATE DATABASE IF NOT EXISTS doctorat_registrations;

-- Se connecter à chaque base et créer les schémas si nécessaire
\c doctorat_users;
-- Les tables seront créées par Hibernate

\c doctorat_registrations;
-- Les tables seront créées par Hibernate
