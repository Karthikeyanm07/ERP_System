# deploy_production.ps1
# Automates the merge and push process for production deployment

Write-Host "🚀 Starting Production Deployment..." -ForegroundColor Cyan

# 1. Fetch latest changes
Write-Host "fetching latest changes..."
git fetch origin

# 2. Switch to main (Production Branch)
Write-Host "Switching to main branch..."
git checkout main
if ($LASTEXITCODE -ne 0) { Write-Error "Failed to checkout main"; exit }

# 3. Pull latest main (to avoid conflicts)
Write-Host "Pulling latest main..."
git pull origin main

# 4. Merge the Feature Branch
# CHANGE THIS if your feature branch has a different name
$FeatureBranch = "ui/sidebar-improvements" 
Write-Host "Merging $FeatureBranch into main..."
git merge $FeatureBranch

if ($LASTEXITCODE -ne 0) { 
    Write-Error "Merge failed! Please resolve conflicts manually."
    exit 
}

# 5. Push to Production
Write-Host "Pushing to GitHub (Triggering Render Deploy)..."
git push origin main

# 6. Return to Feature Branch
Write-Host "Returning to $FeatureBranch..."
git checkout $FeatureBranch

Write-Host "✅ Deployment Triggered Successfully!" -ForegroundColor Green
Write-Host "Go to your Render Dashboard to watch the build."
