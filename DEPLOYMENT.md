# Deployment Guide

This guide provides step-by-step instructions for deploying the URL Shortener application to various free hosting platforms.

## Prerequisites

- GitHub repository with your code
- Account on your chosen deployment platform
- Basic understanding of environment variables

---

## Option 1: Railway (Recommended - Easiest)

### Why Railway?
- Free tier: $5 credit/month (sufficient for hobby projects)
- Built-in PostgreSQL database
- Automatic deployments from GitHub
- Simple configuration
- No credit card required initially

### Step-by-Step Deployment

#### 1. Prepare Your Repository
Ensure your code is pushed to GitHub. The project already includes:
- ✅ `Dockerfile` (optimized for cloud deployment)
- ✅ `server.port=${PORT:8080}` in `application.properties`
- ✅ Environment variable configuration

#### 2. Deploy to Railway

1. **Sign up:**
   - Go to [railway.app](https://railway.app)
   - Click "Start a New Project"
   - Sign in with GitHub OAuth

2. **Create Project:**
   - Click "New Project"
   - Select "Deploy from GitHub repo"
   - Choose your repository
   - Railway will auto-detect the Dockerfile

3. **Add PostgreSQL Database:**
   - In your project dashboard, click "+ New"
   - Select "Database" → "Add PostgreSQL"
   - Railway will automatically create a PostgreSQL instance

4. **Configure Environment Variables:**
   - Go to your app service → "Variables" tab
   - Railway automatically provides database connection variables:
     - `DATABASE_URL` (auto-set by Railway)
     - `DATABASE_USERNAME` (auto-set by Railway)
     - `DATABASE_PASSWORD` (auto-set by Railway)
   - Add these additional variables:
     - `SHORTENER_BASE_URL`: Set to your Railway app URL (e.g., `https://your-app-name.up.railway.app`)
       - You can find this URL in the app's "Settings" → "Domains"
     - `SHORTENER_SLUG_LENGTH`: `8` (optional, defaults to 8)
   - Note: `PORT` is automatically set by Railway, no need to configure it

5. **Link Database to App:**
   - In your app service, go to "Variables" tab
   - Railway should automatically inject database connection variables
   - If not, you can reference them using Railway's variable syntax: `${POSTGRES_URL}`

6. **Deploy:**
   - Railway will automatically build and deploy when you push to your main branch
   - Or click "Deploy" in the dashboard to trigger a manual deployment
   - Monitor the build logs in the "Deployments" tab

7. **Get Your App URL:**
   - Go to app "Settings" → "Domains"
   - Railway provides a free `.up.railway.app` domain
   - Update `SHORTENER_BASE_URL` with this domain if you haven't already

#### 3. Custom Domain (Optional)

1. In Railway dashboard → App Settings → Domains
2. Click "Add Domain"
3. Enter your custom domain
4. Follow Railway's DNS configuration instructions
5. Update `SHORTENER_BASE_URL` environment variable to match

#### 4. Verify Deployment

- Test API: `https://your-app.up.railway.app/api/urls`
- Test Swagger UI: `https://your-app.up.railway.app/swagger-ui.html`
- Test redirect: Create a short URL and verify it redirects correctly

---

## Option 2: Render (Free Tier)

### Why Render?
- Free tier available (spins down after 15 min inactivity)
- Built-in PostgreSQL (free for 90 days, then $7/month)
- GitHub auto-deploy
- Simple setup

### Step-by-Step Deployment

#### 1. Prepare Your Repository
Same as Railway - ensure code is pushed to GitHub.

#### 2. Deploy to Render

1. **Sign up:**
   - Go to [render.com](https://render.com)
   - Sign up with GitHub

2. **Create Web Service:**
   - Click "New +" → "Web Service"
   - Connect your GitHub repository
   - Select your repository

3. **Configure Build Settings:**
   - **Name:** `url-shortener` (or your preferred name)
   - **Region:** Choose closest to you
   - **Branch:** `main` (or your default branch)
   - **Root Directory:** Leave empty (or `.` if needed)
   - **Runtime:** `Docker`
   - **Build Command:** (leave empty - Docker handles this)
   - **Start Command:** (leave empty - Docker handles this)

4. **Add PostgreSQL Database:**
   - Click "New +" → "PostgreSQL"
   - **Name:** `url-shortener-db`
   - **Database:** `url_shortener`
   - **User:** `url_shortener_app`
   - **Region:** Same as your web service
   - Click "Create Database"
   - **Note:** Free tier databases expire after 90 days

5. **Configure Environment Variables:**
   - In your web service → "Environment" tab
   - Add these variables:
     - `DATABASE_URL`: Get from PostgreSQL service → "Connections" tab → "Internal Database URL"
       - Format: `jdbc:postgresql://host:port/database`
     - `DATABASE_USERNAME`: From PostgreSQL service (usually `url_shortener_app`)
     - `DATABASE_PASSWORD`: From PostgreSQL service → "Connections" tab
     - `SHORTENER_BASE_URL`: Your Render app URL (e.g., `https://url-shortener.onrender.com`)
     - `SHORTENER_SLUG_LENGTH`: `8` (optional)
   - `PORT` is automatically set by Render

6. **Link Database:**
   - In your web service → "Environment" tab
   - Click "Link Database" and select your PostgreSQL instance
   - Render will automatically add connection variables

7. **Deploy:**
   - Click "Create Web Service"
   - Render will build and deploy automatically
   - First deployment may take 5-10 minutes

8. **Important Notes:**
   - Free tier services **sleep after 15 minutes of inactivity**
   - First request after sleep takes ~30 seconds (cold start)
   - Consider upgrading to paid plan for always-on service

#### 3. Verify Deployment

- Test API: `https://your-app.onrender.com/api/urls`
- Test Swagger UI: `https://your-app.onrender.com/swagger-ui.html`
- Wait for cold start if service was sleeping

---

## Option 3: Fly.io (Free Tier)

### Why Fly.io?
- Generous free tier (3 shared VMs)
- PostgreSQL via Fly Postgres (free tier available)
- Global edge deployment
- More control, slightly more complex

### Step-by-Step Deployment

#### 1. Install Fly CLI

**Windows (PowerShell):**
```powershell
iwr https://fly.io/install.ps1 -useb | iex
```

**macOS/Linux:**
```bash
curl -L https://fly.io/install.sh | sh
```

Verify installation:
```bash
fly version
```

#### 2. Sign Up and Login

```bash
fly auth signup
# Or if you already have an account:
fly auth login
```

#### 3. Initialize Your App

From your project root:
```bash
fly launch
```

This will:
- Detect your Dockerfile
- Ask for app name (or generate one)
- Ask for region (choose closest to you)
- Ask if you want a PostgreSQL database (say yes)
- Generate `fly.toml` configuration file

#### 4. Configure PostgreSQL

If you didn't create a database during `fly launch`:

```bash
fly postgres create --name url-shortener-db
```

Get connection details:
```bash
fly postgres connect -a url-shortener-db
```

#### 5. Set Environment Variables

```bash
# Get your app URL
fly status

# Set environment variables
fly secrets set DATABASE_URL="jdbc:postgresql://host:port/database"
fly secrets set DATABASE_USERNAME="your-username"
fly secrets set DATABASE_PASSWORD="your-password"
fly secrets set SHORTENER_BASE_URL="https://your-app.fly.dev"
fly secrets set SHORTENER_SLUG_LENGTH="8"
```

**Note:** For PostgreSQL connection, you may need to attach the database:
```bash
fly postgres attach --app your-app-name url-shortener-db
```

This automatically sets `DATABASE_URL` as a secret.

#### 6. Deploy

```bash
fly deploy
```

Monitor deployment:
```bash
fly logs
```

#### 7. Verify Deployment

- Test API: `https://your-app.fly.dev/api/urls`
- Test Swagger UI: `https://your-app.fly.dev/swagger-ui.html`

#### 8. View/Edit Configuration

Edit `fly.toml` to customize:
- Memory limits
- CPU allocation
- Health checks
- Scaling

---

## Option 4: Hybrid Approach (App + External Database)

This approach uses a free app hosting platform with a separate free PostgreSQL database.

### Database Options

#### Supabase (Recommended)
- **Free tier:** 500 MB database, 2 GB bandwidth
- **Sign up:** [supabase.com](https://supabase.com)
- **Steps:**
  1. Create new project
  2. Go to Settings → Database
  3. Copy connection string
  4. Format: `jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres`

#### Neon
- **Free tier:** 3 GB storage, unlimited projects
- **Sign up:** [neon.tech](https://neon.tech)
- **Steps:**
  1. Create new project
  2. Copy connection string from dashboard
  3. Format: `jdbc:postgresql://ep-xxxxx.us-east-2.aws.neon.tech/dbname`

#### ElephantSQL
- **Free tier:** 20 MB database
- **Sign up:** [elephantsql.com](https://www.elephantsql.com)
- **Steps:**
  1. Create new instance
  2. Copy connection details
  3. Format: `jdbc:postgresql://host:port/database`

### Deployment Steps

1. **Create Database:**
   - Choose one of the above providers
   - Create a new database instance
   - Note the connection details

2. **Deploy App:**
   - Use Railway, Render, or Fly.io (follow their respective guides above)
   - Instead of using their built-in PostgreSQL, use your external database

3. **Configure Environment Variables:**
   - `DATABASE_URL`: Connection string from your database provider
   - `DATABASE_USERNAME`: From database provider
   - `DATABASE_PASSWORD`: From database provider
   - `SHORTENER_BASE_URL`: Your app's URL
   - `SHORTENER_SLUG_LENGTH`: `8` (optional)

---

## Environment Variables Reference

All platforms require these environment variables:

| Variable | Description | Example | Required |
|----------|-------------|---------|----------|
| `DATABASE_URL` | PostgreSQL connection string | `jdbc:postgresql://host:5432/dbname` | Yes |
| `DATABASE_USERNAME` | Database username | `url_shortener_app` | Yes |
| `DATABASE_PASSWORD` | Database password | `your-password` | Yes |
| `SHORTENER_BASE_URL` | Base URL for short links | `https://your-app.com` | Yes |
| `SHORTENER_SLUG_LENGTH` | Length of auto-generated slugs | `8` | No (default: 8) |
| `PORT` | Server port | `8080` | No (auto-set by platforms) |

---

## Post-Deployment Checklist

- [ ] Test API endpoint: `POST /api/urls`
- [ ] Test metadata retrieval: `GET /api/urls/{slug}`
- [ ] Test redirect: `GET /{slug}`
- [ ] Verify Swagger UI: `/swagger-ui.html`
- [ ] Check database persistence (create a link, restart app, verify it still exists)
- [ ] Monitor logs for errors
- [ ] Test with different expiration dates
- [ ] Verify hit count increments on redirects
- [ ] Set up custom domain (optional)
- [ ] Configure HTTPS (usually automatic)

---

## Troubleshooting

### Database Connection Issues

**Symptom:** App fails to start with database connection errors

**Solutions:**
- Verify `DATABASE_URL` format: `jdbc:postgresql://host:port/database`
- Check database credentials are correct
- Ensure database is accessible from your app's network
- For external databases, check firewall/whitelist settings
- Verify database is running and healthy

### Port Issues

**Symptom:** App doesn't start or can't bind to port

**Solutions:**
- Don't manually set `PORT` - platforms set it automatically
- Verify `server.port=${PORT:8080}` in `application.properties`
- Check platform logs for port binding errors

### Short URLs Not Working

**Symptom:** Redirects return 404 or wrong URLs

**Solutions:**
- Verify `SHORTENER_BASE_URL` matches your actual app URL
- Ensure `SHORTENER_BASE_URL` includes protocol (`https://`)
- Check that the slug exists in the database
- Verify redirect endpoint is accessible

### Build Failures

**Symptom:** Docker build fails on platform

**Solutions:**
- Check Dockerfile syntax
- Verify Java 21 compatibility
- Review build logs for specific errors
- Test Docker build locally: `docker build -t url-shortener .`

### Cold Start Issues (Render)

**Symptom:** First request takes 30+ seconds

**Solutions:**
- This is normal for Render's free tier (service sleeps after 15 min)
- Consider upgrading to paid plan for always-on service
- Use a service that doesn't sleep (Railway, Fly.io)

---

## Cost Comparison

| Platform | App Hosting | Database | Monthly Cost | Notes |
|----------|-------------|----------|--------------|-------|
| Railway | $5 credit/month | Included | Free (within credit) | Best for ease of use |
| Render | Free (sleeps) | Free 90 days, then $7 | $0-7/month | Good for testing |
| Fly.io | Free (3 VMs) | Free tier available | $0/month | Best for always-on free |
| Hybrid | Varies | Free (Supabase/Neon) | $0/month | Most flexible |

---

## Migration Between Platforms

If you need to migrate from one platform to another:

1. **Export Database:**
   ```bash
   pg_dump -h host -U user -d database > backup.sql
   ```

2. **Deploy to New Platform:**
   - Follow the new platform's deployment guide
   - Set up database (new instance or import backup)

3. **Update Environment Variables:**
   - Configure all required variables on new platform
   - Update `SHORTENER_BASE_URL` to new app URL

4. **Import Data (if needed):**
   ```bash
   psql -h host -U user -d database < backup.sql
   ```

5. **Update DNS/Custom Domain:**
   - Point custom domain to new platform
   - Or update any hardcoded URLs

---

## Additional Resources

- [Railway Documentation](https://docs.railway.app)
- [Render Documentation](https://render.com/docs)
- [Fly.io Documentation](https://fly.io/docs)
- [Spring Boot Deployment Guide](https://spring.io/guides/gs/spring-boot-for-azure/)

---

## Support

For issues specific to:
- **Platform deployment:** Check platform's documentation and support
- **Application code:** Review project README.md
- **Database issues:** Check PostgreSQL logs and connection settings

