# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Communication Language

**IMPORTANT**: Always respond in Chinese (中文) when interacting with the user, unless explicitly requested otherwise.

## Project Overview

**CFGs (Community Fresh Group-buying System)** is a campus food delivery platform with four user roles:
- **Students** - browse merchants, place orders, use coupons, join group orders
- **Merchants** - manage dishes, orders, coupons, and statistics
- **Riders** - accept delivery orders, track earnings
- **Admins** - manage users, merchants, audit rider applications

## Architecture

### Backend (Spring Boot + MyBatis-Plus)
- **Entry point**: `CfgsApplication.java` (`@MapperScan("com.community.cfgs.mapper")`)
- **Configuration**: `application.yml` - MySQL on port 8080, file upload to `./img`
- **Stack**: Spring Boot 2.7.18, MyBatis-Plus 3.5.3.1, MySQL 8, BCrypt for passwords

### Frontend (Vue 3 + Vite + Element Plus + Pinia)
- **Router**: Role-based routing with auth guards in `router/index.js`
- **State**: Pinia stores in `stores/` (user, cart, order, delivery)
- **API**: Axios instance at `axios/request.js` with interceptors for `X-User-Id` header
- **Base URL**: `http://localhost:8080/api`
- **Stack**: Vue 3.5, Vite 6, Element Plus, Vue Router 4, Pinia 3, ECharts, Tailwind CSS
- **Icons**: Uses `iconify-icon` for Lucide icons throughout the UI
- **Image handling**: `getImageUrl()` utility in `utils/imageUrl.js` handles local image paths

### Key Architecture Patterns

**User/Rider Role Handling**: Riders are stored as `role=student` with `isRider=true`. The frontend `useUserStore` handles role switching between student and rider views. When registering as a rider, if a student account with the same phone exists, it upgrades the account to `isRider=true` instead of creating a new user.

**Password Security**: New registrations use BCrypt encryption. The login in `UserService.login()` validates passwords using BCrypt but falls back to plain text comparison for legacy accounts (passwords not starting with `$2`).

**API Response Format**: All endpoints return `Result<T>` with `code`, `message`, and `data`. The axios interceptor unwraps `data` when `code === 200`.

**Group Orders**: Multi-step flow - user creates group order → others join → merchant accepts → system generates individual orders for each member. Status transitions: `open` → `full` → `accepted` (or `cancelled` at any point before accepted).

**File Upload**: Backend uploads to `./img` directory (relative to JAR location), max 10MB. Frontend uses multipart/form-data for dish images and avatars.

## Common Commands

### Backend
```bash
# Build
mvn clean package

# Run (from springboot directory)
mvn spring-boot:run

# Skip tests during build
mvn clean package -DskipTests
```

### Frontend
```bash
# Install dependencies
npm install

# Dev server
npm run dev

# Build for production
npm run build
```

### Database
```bash
# Execute migration scripts via Navicat:
# 1. Open Query Editor
# 2. Paste SQL from docs/db/*.sql
# 3. Run (F5)
```

## Important File Locations

| Purpose | Location |
|---------|----------|
| Backend entities | `springboot/src/main/java/com/community/cfgs/entity/` |
| Backend services | `springboot/src/main/java/com/community/cfgs/service/` |
| Backend controllers | `springboot/src/main/java/com/community/cfgs/controller/` |
| Backend DTOs | `springboot/src/main/java/com/community/cfgs/dto/` |
| Backend common | `springboot/src/main/java/com/community/cfgs/common/` (Result, GlobalExceptionHandler) |
| DB migrations | `springboot/docs/db/*.sql` |
| Frontend views | `vue/src/views/{Student,Merchant,Rider,Admin,Login}/` |
| Frontend stores | `vue/src/stores/*.js` |
| Frontend router | `vue/src/router/index.js` |
| Frontend layouts | `vue/src/views/{Student,Merchant,Rider,Admin}Layout.vue` |
| Frontend components | `vue/src/components/{common,charts,delivery,order}/` |
| Frontend utils | `vue/src/utils/{imageUrl.js,merchantCategories.js}` |

## Role-Specific Notes

**Students**: Can apply to become riders via `RiderApply.vue`. Approval creates a `RiderApplication` record that admins review in `RiderAudit.vue`. Students can switch between student and rider views via role toggle in the user store.

**Merchants**: Linked to users via `merchant.userId`. Shop status (`isOpen`) defaults to `false` for new registrations pending admin approval.

**Riders**: Deliveries tracked in `RiderInfo` with `status` field (pending/approved/rejected). Vehicle info and ID card stored during registration. Riders are stored as `role=student` with `isRider=true` in the users table.

**Admins**: Full access to all user management via `UserManage.vue` and merchant audit via `MerchantList.vue`.

## Database Migration

Migration scripts are in `springboot/docs/db/*.sql`. Execute via Navicat or MySQL client:
- `add_rider_info_fields.sql` - Rider registration fields
- `add_group_order_cancel_fields.sql` - Group order cancellation fields
- `database_optimization.sql` - Indexes and constraints (skip sections 1.1-1.4 if above already run)

## Group Order Status Flow

`open` → `full` (when currentCount reaches targetCount) → `accepted` (merchant confirms) → individual orders generated
- Can be `cancelled` at any point before `accepted`
- `cancelReason` values: `timeout`, `expired`, `manual`, `initiator_cancel`, `merchant_cancel`, `admin_cancel`

## Frontend Utilities

**Merchant Categories**: `parseMerchantCategories()` in `utils/merchantCategories.js` handles category parsing from multiple formats - JSON arrays, double-encoded JSON strings, or comma-separated values. Always use this utility when working with `merchant.categories` field.

**Image URLs**: `getImageUrl()` in `utils/imageUrl.js` converts relative paths to full URLs for display.
