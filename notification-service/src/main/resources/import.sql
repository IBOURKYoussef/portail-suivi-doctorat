INSERT INTO email_templates (template_code, subject, body, active, created_at, updated_at) VALUES
                                                                                               ('REGISTRATION_SUBMITTED', 'Soumission de votre inscription - Portail Doctorat',
                                                                                                '<p>Bonjour {{userName}},</p>
                                                                                                <p>Votre inscription au doctorat a été soumise avec succès.</p>
                                                                                                <p>Numéro d''inscription: {{registrationId}}</p>
                                                                                                <p>Nous vous informerons des prochaines étapes.</p>
                                                                                                <p>Cordialement,<br>Équipe Portail Doctorat</p>',
                                                                                                true, NOW(), NOW()),

                                                                                               ('REGISTRATION_APPROVED_BY_DIRECTOR', 'Votre inscription a été approuvée par le directeur',
                                                                                                '<p>Bonjour {{userName}},</p>
                                                                                                <p>Votre inscription a été approuvée par votre directeur de thèse.</p>
                                                                                                <p>Prochaine étape: validation administrative.</p>
                                                                                                <p>Cordialement,<br>Équipe Portail Doctorat</p>',
                                                                                                true, NOW(), NOW()),

                                                                                               ('REGISTRATION_APPROVED_BY_ADMIN', 'Félicitations - Votre inscription est validée',
                                                                                                '<p>Bonjour {{userName}},</p>
                                                                                                <p>Votre inscription au doctorat a été validée définitivement.</p>
                                                                                                <p>Bienvenue dans le programme doctoral!</p>
                                                                                                <p>Cordialement,<br>Équipe Portail Doctorat</p>',
                                                                                                true, NOW(), NOW());