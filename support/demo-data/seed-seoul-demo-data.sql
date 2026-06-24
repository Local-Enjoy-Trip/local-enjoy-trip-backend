-- Seoul demo seed data for local/dev environments.
-- Source policy:
-- - Attraction names, addresses, coordinates, and images are reused from the local attractions table.
-- - Demo notes are synthetic copy written for product demos; they are not scraped user reviews.
-- - Re-running this script replaces only rows with the deterministic demo prefixes/ids below.

begin;

insert into members (
    name,
    nickname,
    email,
    password,
    role,
    created_at,
    updated_at
)
values
    (
        '서울 데이터 크루',
        '서울크루',
        'demo-seoul-crew@dongnepin.local',
        '{noop}demo-password',
        'USER',
        current_timestamp - interval '12 days',
        current_timestamp
    ),
    (
        '동네핀 에디터',
        '동네핀에디터',
        'demo-editor@dongnepin.local',
        '{noop}demo-password',
        'USER',
        current_timestamp - interval '10 days',
        current_timestamp
    )
on conflict (email) do update
set name = excluded.name,
    nickname = excluded.nickname,
    updated_at = current_timestamp;

delete from course_saves
where course_id like 'demo-seoul-%';

delete from courses
where id like 'demo-seoul-%';

delete from note_saves
where note_id in (
    select n.id
    from notes n
    join members m on m.id = n.author_member_id
    where m.email in (
          'demo-seoul-crew@dongnepin.local',
          'demo-editor@dongnepin.local'
      )
);

delete from notes
where id in (
    select n.id
    from notes n
    join members m on m.id = n.author_member_id
    where m.email in (
          'demo-seoul-crew@dongnepin.local',
          'demo-editor@dongnepin.local'
      )
);

with note_seed (
    author_email,
    attraction_id,
    title,
    content,
    category,
    created_offset
) as (
    values
        (
            'demo-seoul-crew@dongnepin.local',
            126508,
            '서울 데모 | 경복궁 오전 산책',
            '광화문 쪽에서 천천히 들어오면 동선이 편해요. 사진은 근정전 앞보다 측면 담장 길이 훨씬 덜 붐빕니다.',
            'TIP',
            interval '9 days 2 hours'
        ),
        (
            'demo-editor@dongnepin.local',
            775394,
            '서울 데모 | 광화문광장 만남 포인트',
            '지하철 출구에서 바로 합류하기 좋아서 첫 만남 장소로 잡기 좋습니다. 해 질 무렵에는 광장 조명이 예뻐요.',
            'BEST',
            interval '8 days 23 hours'
        ),
        (
            'demo-seoul-crew@dongnepin.local',
            2037020,
            '서울 데모 | 서촌 골목 메모',
            '큰길보다 안쪽 골목을 따라 걸으면 작은 책방과 카페가 이어져요. 오래 머물 코스라면 점심 이후가 좋아요.',
            'BOOK',
            interval '8 days 7 hours'
        ),
        (
            'demo-editor@dongnepin.local',
            126537,
            '서울 데모 | 북촌 조용한 시간',
            '생활 공간이 섞인 동네라 목소리를 낮추고 짧게 머무는 동선이 좋아요. 오전 일찍 가면 훨씬 한산합니다.',
            'TIP',
            interval '7 days 18 hours'
        ),
        (
            'demo-seoul-crew@dongnepin.local',
            126509,
            '서울 데모 | 덕수궁 돌담길',
            '시청 쪽에서 시작해서 정동길로 빠지면 짧지만 분위기가 좋아요. 비 온 뒤 산책 코스로 추천합니다.',
            'BEST',
            interval '7 days 4 hours'
        ),
        (
            'demo-editor@dongnepin.local',
            129507,
            '서울 데모 | 청계천 야간 동선',
            '밤에는 물길을 따라 걷기 좋아요. 동대문 쪽으로 이어 붙이면 부담 없는 저녁 산책 코스가 됩니다.',
            'TIP',
            interval '6 days 21 hours'
        ),
        (
            'demo-seoul-crew@dongnepin.local',
            1906334,
            '서울 데모 | 동대문 야경 포인트',
            '건물 외곽을 한 바퀴 돌면 사진 포인트가 계속 바뀝니다. 실내 전시까지 보려면 시간을 넉넉히 잡으세요.',
            'BEST',
            interval '6 days 3 hours'
        ),
        (
            'demo-editor@dongnepin.local',
            129501,
            '서울 데모 | 낙산공원 성곽길',
            '오르막이 있지만 전망이 좋아요. 해 질 무렵 올라가서 성곽 조명이 켜지는 시간을 맞추면 좋습니다.',
            'TIP',
            interval '5 days 22 hours'
        ),
        (
            'demo-seoul-crew@dongnepin.local',
            126747,
            '서울 데모 | 남산골한옥마을 쉬는 자리',
            '도심 한가운데인데도 잠깐 숨 고르기 좋아요. 남산 코스 시작 전에 들르기 괜찮습니다.',
            'UNCATEGORIZED',
            interval '5 days 8 hours'
        ),
        (
            'demo-editor@dongnepin.local',
            126535,
            '서울 데모 | 남산서울타워 노을',
            '전망을 보려면 날씨가 맑은 날을 고르는 게 좋아요. 케이블카 대기 시간을 생각해서 여유 있게 움직이세요.',
            'TIP',
            interval '4 days 20 hours'
        ),
        (
            'demo-seoul-crew@dongnepin.local',
            2745594,
            '서울 데모 | 성수연방 실내 쉬어가기',
            '성수 일대를 오래 걷다가 잠깐 쉬어가기 좋아요. 비 오는 날 대체 동선으로도 넣기 편합니다.',
            'BEST',
            interval '4 days 2 hours'
        ),
        (
            'demo-editor@dongnepin.local',
            822861,
            '서울 데모 | 한강 바람 체크',
            '강변은 생각보다 바람이 세요. 돗자리보다 가벼운 겉옷을 먼저 챙기는 게 만족도가 높았습니다.',
            'TIP',
            interval '3 days 18 hours'
        ),
        (
            'demo-seoul-crew@dongnepin.local',
            1059479,
            '서울 데모 | 여의도한강공원 피크닉',
            '해가 너무 높을 때보다 늦은 오후가 좋아요. 음식 픽업 동선을 먼저 정하면 자리가 훨씬 편합니다.',
            'TIP',
            interval '3 days 1 hours'
        ),
        (
            'demo-editor@dongnepin.local',
            970138,
            '서울 데모 | 반포한강공원 야경',
            '야경 목적이면 너무 일찍 도착하지 않아도 됩니다. 강변 조명이 켜지는 시간대가 사진에 잘 나와요.',
            'BEST',
            interval '2 days 19 hours'
        ),
        (
            'demo-seoul-crew@dongnepin.local',
            781031,
            '서울 데모 | 홍대 시작점',
            '사람이 많은 코스라 첫 장소를 명확히 정하는 게 좋아요. 연남으로 넘어가면 분위기가 조금 차분해집니다.',
            'MUSIC',
            interval '2 days 7 hours'
        ),
        (
            'demo-editor@dongnepin.local',
            2499807,
            '서울 데모 | 망원시장 간식 루트',
            '시장 안쪽은 붐비니 먹을 것만 빠르게 고르고 한강 쪽으로 빠지는 동선이 편합니다.',
            'TIP',
            interval '1 days 22 hours'
        ),
        (
            'demo-seoul-crew@dongnepin.local',
            126532,
            '서울 데모 | 올림픽공원 넓은 산책',
            '동선이 넓어서 가족 코스로 좋아요. 처음부터 전부 보려고 하기보다 한두 구역만 정하면 덜 지칩니다.',
            'TIP',
            interval '1 days 6 hours'
        ),
        (
            'demo-editor@dongnepin.local',
            2492348,
            '서울 데모 | 서울스카이 전망',
            '송파 쪽 코스의 하이라이트로 두기 좋아요. 날씨가 흐리면 주변 실내 코스로 바꿀 수 있게 잡았습니다.',
            'BEST',
            interval '20 hours'
        ),
        (
            'demo-seoul-crew@dongnepin.local',
            129703,
            '서울 데모 | 국립중앙박물관 느린 관람',
            '전시를 다 보려 하기보다 관심 있는 관 하나를 정하면 훨씬 좋아요. 이촌 산책과 붙이기 좋습니다.',
            'BOOK',
            interval '14 hours'
        ),
        (
            'demo-editor@dongnepin.local',
            2589349,
            '서울 데모 | 서울식물원 비 오는 날',
            '실내외를 함께 쓸 수 있어서 날씨가 애매한 날에 좋습니다. 사진보다 산책 만족도가 높은 편이에요.',
            'TIP',
            interval '8 hours'
        )
)
insert into notes (
    author_member_id,
    title,
    content,
    category,
    visibility,
    location,
    latitude,
    longitude,
    region_name,
    status,
    created_at
)
select m.id,
       s.title,
       s.content,
       s.category,
       'PUBLIC',
       a.location,
       ST_Y(a.location),
       ST_X(a.location),
       concat_ws(' ', split_part(a.addr1, ' ', 1), split_part(a.addr1, ' ', 2)),
       'ACTIVE',
       current_timestamp - s.created_offset
from note_seed s
join members m on m.email = s.author_email
join attractions a on a.id = s.attraction_id
where a.status = 'ACTIVE'
  and a.deleted_at is null
  and a.duplicate_of_attraction_id is null
  and a.location is not null;

with course_seed (
    id,
    owner_email,
    title,
    region_name,
    description,
    first_attraction_id,
    curation_order,
    created_offset
) as (
    values
        (
            'demo-seoul-palace-halfday',
            'demo-editor@dongnepin.local',
            '궁궐과 서촌 반나절 산책',
            '서울 종로구',
            '경복궁에서 시작해 광화문광장, 서촌, 북촌으로 이어지는 서울 대표 도심 산책 코스입니다.',
            126508,
            1,
            interval '6 days'
        ),
        (
            'demo-seoul-jongno-evening',
            'demo-editor@dongnepin.local',
            '정동부터 낙산까지 저녁 산책',
            '서울 중구·종로구',
            '덕수궁 돌담길과 청계천, 동대문, 낙산공원 야경을 이어 붙인 가벼운 저녁 코스입니다.',
            126509,
            2,
            interval '5 days 12 hours'
        ),
        (
            'demo-seoul-namsan-classic',
            'demo-seoul-crew@dongnepin.local',
            '남산 클래식 전망 코스',
            '서울 중구·용산구',
            '한옥마을에서 남산으로 올라 서울 전망을 보는 첫 방문자용 클래식 코스입니다.',
            126747,
            3,
            interval '5 days'
        ),
        (
            'demo-seoul-seongsu-hangang',
            'demo-seoul-crew@dongnepin.local',
            '성수 감성 골목과 한강 바람',
            '서울 성동구',
            '성수 실내 공간과 서울숲 인근 골목, 한강 산책을 묶은 주말 오후 코스입니다.',
            2745594,
            null,
            interval '4 days 9 hours'
        ),
        (
            'demo-seoul-hangang-sunset',
            'demo-editor@dongnepin.local',
            '한강 노을 따라가기',
            '서울 영등포구·용산구·서초구',
            '여의도에서 시작해 이촌, 반포까지 한강변 노을과 야경을 즐기는 코스입니다.',
            1059479,
            null,
            interval '3 days 16 hours'
        ),
        (
            'demo-seoul-mapo-local',
            'demo-seoul-crew@dongnepin.local',
            '홍대·연남·망원 로컬 데이',
            '서울 마포구',
            '홍대에서 시작해 연남 골목, 망원시장, 난지한강공원으로 이어지는 로컬 산책 코스입니다.',
            781031,
            null,
            interval '2 days 15 hours'
        ),
        (
            'demo-seoul-songpa-family',
            'demo-editor@dongnepin.local',
            '송파 가족 나들이 코스',
            '서울 송파구',
            '올림픽공원, 서울스카이, 롯데월드, 잠실한강공원을 하루에 보기 좋게 묶은 가족 코스입니다.',
            126532,
            null,
            interval '1 days 11 hours'
        ),
        (
            'demo-seoul-museum-garden',
            'demo-seoul-crew@dongnepin.local',
            '박물관과 식물원 차분한 하루',
            '서울 용산구·강서구',
            '국립중앙박물관과 이촌 산책, 서울식물원을 느리게 둘러보는 비 오는 날 대체 코스입니다.',
            129703,
            null,
            interval '18 hours'
        )
)
insert into courses (
    id,
    owner_member_id,
    title,
    region_name,
    visibility,
    status,
    description,
    cover_image_url,
    curation_section,
    curation_order,
    start_location,
    created_at,
    updated_at
)
select s.id,
       m.id,
       s.title,
       s.region_name,
       'PUBLIC',
       'READY',
       s.description,
       nullif(a.first_image, ''),
       case when s.curation_order is null then null else 'MD_RECOMMENDED' end,
       s.curation_order,
       a.location,
       current_timestamp - s.created_offset,
       current_timestamp
from course_seed s
join members m on m.email = s.owner_email
join attractions a on a.id = s.first_attraction_id;

with item_seed (
    course_id,
    position,
    day,
    item_type,
    attraction_id,
    note_title,
    memo,
    stay_minutes
) as (
    values
        ('demo-seoul-palace-halfday', 1, 1, 'ATTRACTION', 126508, null, '경복궁에서 서울 궁궐 코스를 시작합니다.', 80),
        ('demo-seoul-palace-halfday', 2, 1, 'NOTE', null, '서울 데모 | 광화문광장 만남 포인트', '광장 쪽에서 잠깐 쉬며 다음 동선을 잡습니다.', 20),
        ('demo-seoul-palace-halfday', 3, 1, 'ATTRACTION', 2037020, null, '서촌 골목으로 넘어가 점심과 카페를 붙입니다.', 70),
        ('demo-seoul-palace-halfday', 4, 1, 'NOTE', null, '서울 데모 | 북촌 조용한 시간', '북촌은 짧고 조용하게 둘러봅니다.', 45),

        ('demo-seoul-jongno-evening', 1, 1, 'ATTRACTION', 126509, null, '덕수궁 돌담길에서 저녁 산책을 시작합니다.', 45),
        ('demo-seoul-jongno-evening', 2, 1, 'NOTE', null, '서울 데모 | 청계천 야간 동선', '청계천 물길을 따라 동대문 방향으로 걷습니다.', 50),
        ('demo-seoul-jongno-evening', 3, 1, 'ATTRACTION', 1906334, null, '동대문역사문화공원 야경을 봅니다.', 40),
        ('demo-seoul-jongno-evening', 4, 1, 'NOTE', null, '서울 데모 | 낙산공원 성곽길', '체력이 남으면 낙산공원까지 올라갑니다.', 60),

        ('demo-seoul-namsan-classic', 1, 1, 'NOTE', null, '서울 데모 | 남산골한옥마을 쉬는 자리', '도심 속 한옥마을에서 워밍업합니다.', 40),
        ('demo-seoul-namsan-classic', 2, 1, 'ATTRACTION', 128776, null, '케이블카 대기 시간을 확인하고 올라갑니다.', 30),
        ('demo-seoul-namsan-classic', 3, 1, 'NOTE', null, '서울 데모 | 남산서울타워 노을', '노을 시간대 전망을 코스의 중심으로 둡니다.', 70),
        ('demo-seoul-namsan-classic', 4, 1, 'ATTRACTION', 3067314, null, '예장공원 방향으로 내려오며 마무리합니다.', 35),

        ('demo-seoul-seongsu-hangang', 1, 1, 'NOTE', null, '서울 데모 | 성수연방 실내 쉬어가기', '실내 공간에서 시작해 날씨 영향을 줄입니다.', 45),
        ('demo-seoul-seongsu-hangang', 2, 1, 'ATTRACTION', 2894252, null, '성수 골목을 따라 가볍게 이동합니다.', 40),
        ('demo-seoul-seongsu-hangang', 3, 1, 'ATTRACTION', 3444690, null, '서울숲 인근 카페 거리에서 쉬어갑니다.', 50),
        ('demo-seoul-seongsu-hangang', 4, 1, 'NOTE', null, '서울 데모 | 한강 바람 체크', '강변 산책은 바람과 체감온도를 확인합니다.', 50),

        ('demo-seoul-hangang-sunset', 1, 1, 'NOTE', null, '서울 데모 | 여의도한강공원 피크닉', '늦은 오후 피크닉으로 시작합니다.', 60),
        ('demo-seoul-hangang-sunset', 2, 1, 'ATTRACTION', 3116066, null, '여의도 한강 야경 포인트를 확인합니다.', 30),
        ('demo-seoul-hangang-sunset', 3, 1, 'ATTRACTION', 970636, null, '이촌 쪽으로 이동해 강변 분위기를 바꿉니다.', 45),
        ('demo-seoul-hangang-sunset', 4, 1, 'NOTE', null, '서울 데모 | 반포한강공원 야경', '반포 야경으로 하루를 마무리합니다.', 50),

        ('demo-seoul-mapo-local', 1, 1, 'NOTE', null, '서울 데모 | 홍대 시작점', '홍대입구 근처에서 모여 출발합니다.', 35),
        ('demo-seoul-mapo-local', 2, 1, 'ATTRACTION', 2784040, null, '연남동 골목으로 이동해 산책합니다.', 55),
        ('demo-seoul-mapo-local', 3, 1, 'NOTE', null, '서울 데모 | 망원시장 간식 루트', '시장 간식은 빠르게 고르고 포장합니다.', 45),
        ('demo-seoul-mapo-local', 4, 1, 'ATTRACTION', 127859, null, '난지한강공원에서 피크닉으로 마무리합니다.', 60),

        ('demo-seoul-songpa-family', 1, 1, 'NOTE', null, '서울 데모 | 올림픽공원 넓은 산책', '넓은 공원에서 여유 있게 시작합니다.', 70),
        ('demo-seoul-songpa-family', 2, 1, 'NOTE', null, '서울 데모 | 서울스카이 전망', '날씨가 좋으면 전망을 우선합니다.', 80),
        ('demo-seoul-songpa-family', 3, 1, 'ATTRACTION', 126498, null, '아이와 함께라면 롯데월드 시간을 넉넉히 둡니다.', 120),
        ('demo-seoul-songpa-family', 4, 1, 'ATTRACTION', 970460, null, '잠실한강공원에서 산책하며 마무리합니다.', 45),

        ('demo-seoul-museum-garden', 1, 1, 'NOTE', null, '서울 데모 | 국립중앙박물관 느린 관람', '전시 하나를 정해서 천천히 봅니다.', 90),
        ('demo-seoul-museum-garden', 2, 1, 'ATTRACTION', 970636, null, '이촌한강공원에서 바람을 쐽니다.', 45),
        ('demo-seoul-museum-garden', 3, 1, 'ATTRACTION', 126747, null, '도심 한옥 분위기로 전환합니다.', 40),
        ('demo-seoul-museum-garden', 4, 1, 'NOTE', null, '서울 데모 | 서울식물원 비 오는 날', '날씨가 애매하면 식물원으로 동선을 바꿉니다.', 80)
)
insert into course_items (
    course_id,
    item_type,
    attraction_id,
    note_id,
    position,
    day,
    memo,
    stay_minutes,
    created_at
)
select s.course_id,
       s.item_type,
       s.attraction_id,
       n.id,
       s.position,
       s.day,
       s.memo,
       s.stay_minutes,
       current_timestamp
from item_seed s
left join (
    select distinct on (n.title)
           n.title,
           n.id
    from notes n
    join members m on m.id = n.author_member_id
    where n.title like '서울 데모 | %'
      and m.email in (
          'demo-seoul-crew@dongnepin.local',
          'demo-editor@dongnepin.local'
      )
    order by n.title, n.created_at desc, n.id desc
) n on n.title = s.note_title
where s.item_type = 'ATTRACTION'
   or n.id is not null;

with item_points as (
    select i.course_id,
           i.id,
           i.position,
           coalesce(a.location, n.location) as location
    from course_items i
    left join attractions a on a.id = i.attraction_id
    left join notes n on n.id = i.note_id
    where i.course_id like 'demo-seoul-%'
),
route_lines as (
    select course_id,
           ST_MakeLine(location order by position) as route_line,
           (array_agg(location order by position))[1] as start_location
    from item_points
    where location is not null
    group by course_id
    having count(*) >= 2
)
update courses c
set route_line = r.route_line,
    start_location = r.start_location,
    updated_at = current_timestamp
from route_lines r
where c.id = r.course_id;

with item_points as (
    select i.course_id,
           i.id,
           i.position,
           coalesce(a.location, n.location) as location
    from course_items i
    left join attractions a on a.id = i.attraction_id
    left join notes n on n.id = i.note_id
    where i.course_id like 'demo-seoul-%'
)
insert into course_route_segments (
    course_id,
    from_course_item_id,
    to_course_item_id,
    segment_order,
    travel_mode,
    duration_seconds,
    distance_meters
)
select p1.course_id,
       p1.id,
       p2.id,
       p1.position,
       'WALK',
       greatest(
           300,
           round(ST_Distance(p1.location::geography, p2.location::geography) / 1.2)::integer
       ),
       round(ST_Distance(p1.location::geography, p2.location::geography))::integer
from item_points p1
join item_points p2
  on p2.course_id = p1.course_id
 and p2.position = p1.position + 1
where p1.location is not null
  and p2.location is not null;

insert into course_saves (
    course_id,
    member_id,
    created_at
)
select c.id,
       m.id,
       current_timestamp - interval '3 hours'
from courses c
join members m on m.email in (
    'demo-seoul-crew@dongnepin.local',
    'demo-editor@dongnepin.local'
)
where c.id like 'demo-seoul-%'
  and c.owner_member_id <> m.id
on conflict (course_id, member_id) do nothing;

insert into note_saves (
    note_id,
    member_id,
    created_at
)
select n.id,
       m.id,
       current_timestamp - interval '2 hours'
from notes n
join members author on author.id = n.author_member_id
join members m on m.email in (
    'demo-seoul-crew@dongnepin.local',
    'demo-editor@dongnepin.local'
)
where n.title like '서울 데모 | %'
  and author.email in (
      'demo-seoul-crew@dongnepin.local',
      'demo-editor@dongnepin.local'
  )
  and n.author_member_id <> m.id
on conflict (note_id, member_id) do nothing;

commit;
