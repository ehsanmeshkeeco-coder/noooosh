# راهنمای جامع GitHub Actions و فرآیند CI/CD

این سند راهنمای کامل راه‌اندازی، تنظیمات محرمانه (Secrets)، و اجرای فرآیندهای Continuous Integration (CI) و Continuous Delivery (CD) برای پروژه اندروید **نوش (Noosh)** است.

---

## ۱. فهرست گردش‌کارهای خودکار (Workflows)

پروژه شامل دو پایپ‌لاین مجزا در پوشه `.github/workflows/` می‌باشد:

1. **`ci.yml` (Android CI)**:
   - **هدف**: بررسی صحت کدهای ارسالی، اجرای تست‌های واحد و Robolectric، و اطمینان از کامپایل موفق پروژه.
   - **تریگرها**: Push به شاخه‌های `main`، `master`، `develop` یا ایجاد Pull Request به سمت `main` و `master`.
   - **خروجی**: فایل آرتیفکت APK دیباگ (`noosh-debug-apk`).

2. **`release-apk.yml` (Release APK)**:
   - **هدف**: امضای دیجیتال و ساخت نسخه نهایی قابل انتشار (Signed Release APK) و ایجاد خودکار GitHub Release همراه با یادداشت‌های نسخه.
   - **تریگرها**:
     - دستی از طریق تب Actions (گزینه `workflow_dispatch`).
     - خودکار با Push کردن تگ نسخه (مانند `v1.0.0`).
   - **خروجی**: انتشار فایل APK امضا شده در بخش Releases مخزن گیت‌هاب و ایجاد آرتیفکت دانلود.

---

## ۲. جدول متغیرهای محرمانه مخزن (GitHub Secrets)

برای فعال‌سازی کامل فرآیندهای ساخت و انتشار، متغیرهای زیر باید در مسیر **Settings > Secrets and variables > Actions > Repository secrets** در گیت‌هاب تعریف شوند:

### دسته الف: متغیرهای امضای دیجیتال نسخه نهایی (Android Signing)
| نام Secret | الزامی؟ | توضیحات | مقدار پیش‌فرض تولیدشده |
| :--- | :---: | :--- | :--- |
| `ANDROID_KEYSTORE_BASE64` (یا `RELEASE_KEYSTORE_BASE64`) | **بله (برای Release)** | رشته Base64 فایل کلید امضا (`release.keystore`) | محتوای فایل `release.keystore.base64` |
| `ANDROID_KEYSTORE_PASSWORD` (یا `STORE_PASSWORD`) | **بله (برای Release)** | رمز عبور Keystore | `noosh123456` |
| `ANDROID_KEY_ALIAS` (یا `KEY_ALIAS`) | **بله (برای Release)** | نام مستعار کلید داخل Keystore | `upload` |
| `ANDROID_KEY_PASSWORD` (یا `KEY_PASSWORD`) | **بله (برای Release)** | رمز عبور Alias کلید | `noosh123456` |

### دسته ب: خدمات Firebase و اعلان‌ها
| نام Secret | الزامی؟ | توضیحات | نحوه تولید / مقدار |
| :--- | :---: | :--- | :--- |
| `GOOGLE_SERVICES_JSON` | اختیاری / توصیه شده | محتوای فایل `google-services.json` | دانلود از کنسول Firebase در بخش تنظیمات پروژه برای پکیج `com.aistudio.noosh.water` |

### دسته ج: پایگاه داده ابری و احراز هویت (Clerk & Supabase)

#### متغیرهای محرمانه (Repository Secrets):
در مسیر **Settings > Secrets and variables > Actions > Secrets**:
| نام Secret | الزامی؟ | توضیحات |
| :--- | :---: | :--- |
| `CLERK_PUBLISHABLE_KEY` | **بله** | کلید عمومی Clerk برای ورود و ثبت‌نام کاربران (`pk_test_...` یا `pk_live_...`) |
| `SUPABASE_ANON_KEY` | **بله** | کلید عمومی (anon / public) پروژه سوپابیس برای همگام‌سازی ابری و دیتابیس |
| `SUPABASE_SERVICE_ROLE_KEY` | اختیاری | کلید مدیریتی سطح بالای سوپابیس (فقط در صورت نیاز به دسترسی‌های ویژه) |

#### متغیرهای عمومی مخزن (Repository Variables):
در مسیر **Settings > Secrets and variables > Actions > Variables**:
| نام Variable | الزامی؟ | توضیحات |
| :--- | :---: | :--- |
| `SUPABASE_URL` | **بله** | آدرس اختصاصی پروژه سوپابیس شما (مانند `https://xyz.supabase.co`) |

> **نکته**: پایپ‌لاین‌های `release-apk.yml` و `ci.yml` طوری طراحی شده‌اند که مقدار `SUPABASE_URL` را چه در بخش **Variables** و چه در بخش **Secrets** تعریف کرده باشید، به صورت خودکار شناسایی کرده و مستقیماً به محیط بیلد Gradle و فایل `.env` تزریق می‌نمایند.

---

## ۳. مراحل راه‌اندازی مرحله به مرحله در GitHub

1. وارد مخزن پروژه خود در GitHub شوید.
2. روی تب **Settings** در نوار بالای مخزن کلیک کنید.
3. از منوی سمت چپ، بخش **Secrets and variables** و سپس **Actions** را انتخاب کنید.
4. روی دکمه سبز رنگ **New repository secret** کلیک کنید.
5. به ازای هر متغیر از جدول بالا، نام و مقدار دقیق آن را ثبت نمایید.

---

## ۴. نحوه اجرای دستی ساخت نسخه انتشار (Release APK)

1. به تب **Actions** در مخزن گیت‌هاب بروید.
2. از لیست سمت چپ، گردش‌کار **Release APK** را انتخاب کنید.
3. روی منوی کشویی **Run workflow** کلیک کنید.
4. در صورت تمایل شماره نسخه (مانند `1.0.0`) را وارد کرده و دکمه سبز **Run workflow** را بزنید.
5. پس از اتمام فرآیند، فایل APK در بخش Artifacts قابل دانلود خواهد بود.

---

## ۵. عیب‌یابی خطاهای رایج در پایپ‌لاین

- **خطای `./gradlew: No such file or directory` یا خطا در مرحله Run Unit Tests**:
  - این مشکل به دلیل نبود اسکریپت `gradlew` و `gradle-wrapper.jar` در مخزن بود که اکنون ایجاد و اضافه شد. همچنین اکشن رسمی `gradle/actions/setup-gradle@v4` جهت تنظیم خودکار مجوزهای اجرایی و اعتبارسنجی Wrapper اضافه گردید.
  - نتایج گزارش کامل تست‌ها (`HTML Report`) نیز به عنوان Artifact با نام `test-results` ذخیره می‌شود.
- **خطای Keystore not found یا Decryption failed**:
  - بررسی کنید که رشته `RELEASE_KEYSTORE_BASE64` دقیقاً بدون کاراکترهای شکست خط (Newlines) کپی شده باشد.
- **خطای Keystore was tampered with, or password was incorrect**:
  - بررسی کنید که `STORE_PASSWORD` و `KEY_PASSWORD` دقیقاً همان مقادیری باشند که در دستور `keytool` وارد شده‌اند.
- **خطای No matching client found for package name**:
  - در فایل `google-services.json`، مقدار `package_name` باید دقیقاً برابر با `com.aistudio.noosh.water` باشد.
