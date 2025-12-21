<!-- Copilot / AI agent instructions for My_Safe_Haven_PAMM -->
# Quick onboarding for AI coding agents

This file contains concise, actionable knowledge to be productive on this repo.

1) Big picture
- Backend: Flask app at `Backend/my_safe_haven_back/app.py` exposing a REST API + Socket.IO (port 5050). It handles auth (JWT), havens, posts, chat messages and serves uploaded files from `uploads/`.
- Database: Postgres (docker-compose service `db`). DB init SQL is in `Backend/db/init.sql` and other schema seed files are in `Backend/db/*.sql`.
- Runtime: `docker-compose.yml` composes `db` and `backend`. Backend image is built from `Backend/dockerfile` and uses volume `uploads_data` to persist uploads.

2) Key files and patterns (examples)
- Models: `Backend/my_safe_haven_back/models.py` — SQLAlchemy models (note: `Haven` primary key is named `haven_id`).
- App entry: `Backend/my_safe_haven_back/app.py` — single-file Flask app that initializes `db` from `Backend/my_safe_haven_back/extensions.py` and then imports models.
- Routes: Most API routes live in `app.py`. There is an alternate/legacy `Backend/my_safe_haven_back/routes/routes.py` — be cautious: it shows different model names and may be stale.
- Requirements: `Backend/requirements.txt` lists `eventlet`, `Flask-SocketIO`, `Flask-JWT-Extended`, and `psycopg2-binary` — match these when running locally.

3) Runtime & developer workflows
- Docker (recommended):
  - Start services: `docker-compose up --build` (backend on 5050, db on 5432).
  - DB init: docker-compose mounts `Backend/db/init.sql` into Postgres init directory.
- Local (without Docker):
  - Ensure Postgres is available and env vars match the ones in `docker-compose.yml` (POSTGRES_*). The app uses connection `postgresql://postgres:Nicololo@db:5432/mysafehaven` by default inside container.
  - Run the server: `python Backend/my_safe_haven_back/app.py` (it uses `socketio.run(...)`).
- Migrations: `Flask-Migrate` is installed and `Migrate(app, db)` is used, but the repo currently calls `db.create_all()` on startup — check for missing `migrations/` if planning schema changes.

4) Conventions & gotchas
- JWT identity: tokens store `identity=str(user.id)` and endpoints convert `get_jwt_identity()` to `int` — keep conversions consistent.
- File uploads: `app.config['UPLOAD_FOLDER'] = 'uploads'` and docker-compose maps `uploads_data` to `/app/my_safe_haven_back/uploads`. Use that path when testing file serving (GET `/uploads/<filename>`).
- Naming: some models use `haven_id` as primary key (not `id`) — queries and relationships rely on those names.
- Websockets: server emits `new_message` to room `haven_{haven_id}` and listens for `join_haven`; client examples are in `Backend/README.md`.
- Multiple route sources: `app.py` is authoritative; `routes/routes.py` appears inconsistent/stale — prefer `app.py` when adding endpoints.

5) Integration & testing notes
- External deps: Postgres (image `postgres:17`) and eventlet for SocketIO; ensure `eventlet` is active when running the app with Socket.IO.
- To inspect DB seed data: look under `Backend/db/` (`users.sql`, `havens.sql`, `haven_posts.sql`, `chat_messages.sql`, `subscriptions.sql`).

6) What to change as an AI assistant
- Small fixes: prefer minimal targeted changes (e.g., fix `allowed_file` to return boolean, ensure allowed extensions check is correct).
- When adding endpoints, register them in `app.py` and update `Backend/README.md` examples.
- If modifying schema, add Flask-Migrate scripts (create `migrations/`) instead of relying solely on `db.create_all()`.

7) Contact & follow-ups
- If something is unclear, ask which environment the user runs (Docker vs local Python) and whether to run `docker-compose up` for verification.

---
Please review and tell me any missing repository-specific details you want included.
