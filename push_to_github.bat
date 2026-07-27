@echo off
setlocal
title Geologist - Pandisha code GitHub (bonyeza tu)
cd /d "%~dp0"

REM ==== URL ya repo yako imewekwa hapa (huna haja ya kuandika) ====
set "URL=https://github.com/khamisule/geologist.git"

echo ============================================================
echo   GEOLOGIST - Pandisha Android code kwenda GitHub
echo   Repo: %URL%
echo ============================================================
echo.
where git >nul 2>nul
if errorlevel 1 (
  echo HITILAFU: Git haijapatikana. Sakinisha: https://git-scm.com/download/win
  pause & exit /b 1
)

REM --- Weka jina/email ya Git kama hazipo (ili commit isishindwe) ---
git config user.email >nul 2>nul || git config user.email "geologist@local"
git config user.name  >nul 2>nul || git config user.name  "Geologist"

echo [1/5] git init ...
if not exist ".git" git init
echo [2/5] git add ...
git add -A
echo [3/5] git commit ...
git commit -m "Geologist Android app (full features)"
echo [4/5] branch main + remote ...
git branch -M main
git remote remove origin >nul 2>nul
git remote add origin %URL%
echo.
echo [5/5] git push (--force) ...
echo      Dirisha la KUINGIA GitHub litafunguka kwenye browser - INGIA mara moja.
git push -u origin main --force
if errorlevel 1 goto :fail

echo.
echo ============================================================
echo   IMEFANIKIWA! Code iko GitHub.
echo   Sasa: github.com/khamisule/geologist -^> tab "Actions"
echo         -^> "Build Geologist APK" -^> subiri kijani -^> pakua APK.
echo ============================================================
echo.
pause
exit /b 0

:fail
echo.
echo ============================================================
echo   HITILAFU: push imeshindwa. Angalia:
echo   - Je, uliingia GitHub kwenye dirisha lililofunguka?
echo   - Je, una mtandao?
echo   NAKILI ujumbe wa juu (nyekundu) umpe msaidizi.
echo ============================================================
echo.
pause
exit /b 1
